import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PostService } from '../../services/post.service';
import { CommentService } from '../../../comment/services/comment.service';
import { ReactionService } from '../../../reaction/services/reaction.service';
import { TokenStorageService } from '../../../auth/services/token-storage.service';
import { Post, PostStatus } from '../../models/post.model';
import { Comment, CommentRequest } from '../../../comment/models/comment.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpEventType, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-post-detail',
  templateUrl: './post-detail.component.html',
  styleUrls: ['./post-detail.component.scss']
})
export class PostDetailComponent implements OnInit {
  post: Post | null = null;
  comments: Comment[] = [];
  isLoading = false;
  errorMessage = '';
  commentForm!: FormGroup;
  replyForm!: FormGroup;
  currentUser: any;
  isCommentFormVisible = false;
  postServiceBaseUrl: string = environment.postServiceBaseUrl;
  private commentApiUrl = environment.commentApiUrl;
  commentServiceHostForImages: string;
  
  selectedCommentFile: File | null = null;
  commentUploadProgress = 0;
  commentFileUploadError: string | null = null;
  commentPreviewUrl: string | ArrayBuffer | null = null;
  isUploadingCommentImage = false;
  
  selectedReplyFile: File | null = null;
  replyUploadProgress = 0;
  replyFileUploadError: string | null = null;
  replyPreviewUrl: string | ArrayBuffer | null = null;
  isUploadingReplyImage = false;
  activeReplyParentComment: Comment | null = null;
  
  userPostVotes: Map<number, boolean | null> = new Map();
  isPostVoteLoading: Map<number, boolean> = new Map();
  isCommentVoteLoading: Map<number, boolean> = new Map();
  userCommentVotes: Map<number, boolean | null> = new Map();
  commentVotingState: Map<number, boolean> = new Map();
  
  postStatus = PostStatus;

  @ViewChild('imageModal') imageModal: ElementRef | undefined;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private postService: PostService,
    private commentService: CommentService,
    private reactionService: ReactionService,
    private tokenStorage: TokenStorageService,
    private formBuilder: FormBuilder,
    private http: HttpClient
  ) {
    if (this.commentApiUrl.endsWith('/api')) {
      this.commentServiceHostForImages = this.commentApiUrl.slice(0, -4);
    } else {
      this.commentServiceHostForImages = this.commentApiUrl;
    }
  }

  get isPostAuthor(): boolean {
    return !!this.post && !!this.currentUser && this.post.authorId === this.currentUser.id;
  }

  private findCommentById(commentsToSearch: Comment[], id: number): Comment | null {
    for (const comment of commentsToSearch) {
      if (comment.id === id) {
        return comment;
      }
      if (comment.replies) {
        const foundInReply = this.findCommentById(comment.replies, id);
        if (foundInReply) {
          return foundInReply;
        }
      }
    }
    return null;
  }

  private fetchAndSetReactionStats(commentsToProcess: Comment[]): void {
    if (this.currentUser && commentsToProcess) {
      commentsToProcess.forEach(comment => {

        this.reactionService.getReactionStats(comment.id, 'COMMENT').subscribe(
          response => {
            this.userCommentVotes.set(
              comment.id,
              response.userReaction === 'LIKE' ? true :
              response.userReaction === 'DISLIKE' ? false : null
            );
          },
          error => console.error(`Error loading reaction stats for comment ${comment.id}:`, error)
        );
      });
    }
  }

  ngOnInit(): void {
    this.currentUser = this.tokenStorage.getUser();
    console.log('Current user:', this.currentUser);
    console.log('JWT Token:', this.tokenStorage.getToken());
    
    this.commentForm = this.formBuilder.group({
      content: ['', [Validators.required, Validators.maxLength(500)]],
      imageUrl: ['']
    });

    this.replyForm = this.formBuilder.group({
      content: ['', [Validators.required, Validators.maxLength(500)]],
      imageUrl: ['']
    });

    this.route.paramMap.subscribe(params => {
      const postId = Number(params.get('id'));
      if (postId) {
        this.loadPost(postId);
        this.loadComments(postId);
      } else {
        this.errorMessage = 'Invalid post ID';
      }
    });
  }

  loadPost(postId: number): void {
    this.isLoading = true;
    this.postService.getPostById(postId).subscribe(
      post => {
        this.post = post;
        this.isLoading = false;
        
        if (this.currentUser && postId) {
          this.reactionService.getReactionStats(postId, 'POST').subscribe(
            response => {
              this.userPostVotes.set(postId, response.userReaction === 'LIKE' ? true : response.userReaction === 'DISLIKE' ? false : null);
            },
            error => {
              this.userPostVotes.set(postId, null);
            }
          );
        }
      },
      error => {
        this.errorMessage = error.error?.message || 'Error loading post';
        this.isLoading = false;
      }
    );
  }

  loadComments(postId: number): void {
    this.commentService.getCommentsByPostId(postId).subscribe(
      comments => {
        this.comments = comments;
        this.fetchAndSetReactionStats(this.comments);
      },
      error => {
        this.errorMessage = error.error?.message || 'Error loading comments';
      }
    );
  }

  onCommentFileSelected(event: Event): void {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList && fileList.length > 0) {
      this.selectedCommentFile = fileList[0];
      this.commentFileUploadError = null;
      this.commentUploadProgress = 0;
      this.commentForm.controls['imageUrl'].setValue('');
      this.commentPreviewUrl = null;

      const reader = new FileReader();
      reader.onload = (e) => this.commentPreviewUrl = reader.result;
      reader.readAsDataURL(this.selectedCommentFile);

      this.uploadCommentFile();
    } else {
      this.selectedCommentFile = null;
      this.commentPreviewUrl = null; 
    }
  }

  uploadCommentFile(): void {
    if (!this.selectedCommentFile) {
      return;
    }

    const formData = new FormData();
    formData.append('file', this.selectedCommentFile, this.selectedCommentFile.name);

    this.commentUploadProgress = 0;
    this.isUploadingCommentImage = true;
    this.commentFileUploadError = null;

    const token = this.tokenStorage.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', 'Bearer ' + token);
    }

    this.http.post(`${this.commentApiUrl}/comments/upload-image`, formData, {
      headers: headers,
      reportProgress: true,
      observe: 'events'
    }).subscribe({
      next: (event: any) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.commentUploadProgress = Math.round(100 * event.loaded / event.total);
        } else if (event.type === HttpEventType.Response && event.body) {
          const responseBody = event.body as { imageUrl: string };
          this.commentForm.controls['imageUrl'].setValue(responseBody.imageUrl);
          this.commentPreviewUrl = this.commentServiceHostForImages + responseBody.imageUrl; 
          this.selectedCommentFile = null;
          this.isUploadingCommentImage = false;
          this.commentFileUploadError = null;
        }
      },
      error: (err) => {
        this.commentFileUploadError = err.error?.message || 'File upload failed. Please try again.';
        console.error("Comment file upload error:", err);
        this.commentUploadProgress = 0;
        this.isUploadingCommentImage = false;
        this.selectedCommentFile = null;
      }
    });
  }

  submitComment(): void {
    if (this.commentForm.invalid || !this.post) {
      return;
    }

    if (this.isUploadingCommentImage) {
        this.commentFileUploadError = 'Image is still uploading. Please wait.';
        return;
    }
    if (this.selectedCommentFile && !this.commentForm.controls['imageUrl'].value) {
        this.commentFileUploadError = 'Image upload failed or was not completed. Please try re-uploading or remove the file selection.';
        return;
    }

    const commentRequest: CommentRequest = {
      postId: this.post.id,
      content: this.commentForm.value.content,
      imageUrl: this.commentForm.value.imageUrl || undefined,
      parentId: undefined
    };

    this.commentService.createComment(commentRequest).subscribe(
      comment => {
        this.comments.unshift(comment);
        this.commentForm.reset();
        this.isCommentFormVisible = false;
        this.selectedCommentFile = null;
        this.commentUploadProgress = 0;
        this.commentFileUploadError = null;
        this.commentPreviewUrl = null;
        this.isUploadingCommentImage = false;
        
        if (this.post && this.post.status === PostStatus.JUST_POSTED) {
          this.post.status = PostStatus.FIRST_REACTIONS;
        }
      },
      error => {
        this.errorMessage = error.error?.message || 'Error creating comment';
      }
    );
  }

  toggleReplyForm(comment: Comment): void {
    if (this.isCommentFormVisible) {
    }
    
    this.comments.forEach(c => {
      if (c.id !== comment.id) {
        c.isReplyFormVisible = false;
      }
    });
    comment.isReplyFormVisible = !comment.isReplyFormVisible;
    
    if (comment.isReplyFormVisible) {
      this.activeReplyParentComment = comment;
      this.replyForm.reset();
      this.resetReplyUploadState();
    } else {
      this.activeReplyParentComment = null;
      this.resetReplyUploadState(); 
    }
  }

  submitReply(parentCommentId: number): void {
    if (this.replyForm.invalid || !this.post) {
      return;
    }

    if (this.isUploadingReplyImage) {
      this.replyFileUploadError = 'Image is still uploading. Please wait.';
      return;
    }
    if (this.selectedReplyFile && !this.replyForm.controls['imageUrl'].value) {
      this.replyFileUploadError = 'Image upload failed or was not completed. Please try re-uploading or remove the file selection.';
      return;
    }

    const replyRequest: CommentRequest = {
      postId: this.post.id,
      content: this.replyForm.value.content,
      parentId: parentCommentId,
      imageUrl: this.replyForm.value.imageUrl || undefined
    };

    this.commentService.createComment(replyRequest).subscribe(
      reply => {
        const parentComment = this.comments.find(c => c.id === parentCommentId);
        if (parentComment) {
          if (!parentComment.replies) {
            parentComment.replies = [];
          }
          parentComment.replies.push(reply);
          parentComment.isReplyFormVisible = false;
          parentComment.repliesLoaded = true;
        }
        this.replyForm.reset();
        this.resetReplyUploadState();
        this.activeReplyParentComment = null;
      },
      error => {
        console.error('Error submitting reply:', error);
        this.replyFileUploadError = error.error?.message || (error.error instanceof ErrorEvent ? error.error.message : 'Failed to submit reply.');
      }
    );
  }

  loadReplies(comment: Comment): void {
    if (comment.repliesLoaded || comment.isLoadingReplies) {
      return;
    }
    comment.isLoadingReplies = true;
    this.commentService.getReplies(comment.id).subscribe(
      replies => {
        comment.replies = replies;
        comment.isLoadingReplies = false;
        comment.repliesLoaded = true;
        this.fetchAndSetReactionStats(comment.replies);
      },
      error => {
        console.error('Error loading replies:', error);
        comment.isLoadingReplies = false;
      }
    );
    this.resetCommentUploadState();
  }

  likePost(event: MouseEvent): void {
    console.log('likePost called for post:', this.post ? this.post.id : 'unknown');
    event.preventDefault();
    event.stopPropagation();
    if (!this.post || this.isPostVoteLoading.get(this.post.id)) return;
    if (this.isPostAuthor) return;
    const postId = this.post ? this.post.id : null;
    if (postId === null) return;
    this.isPostVoteLoading.set(postId, true);
    const currentReaction = this.userPostVotes.get(postId) === true ? 'LIKE' : this.userPostVotes.get(postId) === false ? 'DISLIKE' : null;
    this.reactionService.toggleLike(postId, 'POST', currentReaction)
      .subscribe(
        response => {
          if (this.post) {
            this.post.voteCount = response.totalScore;
            this.userPostVotes.set(postId, response.userReaction === 'LIKE' ? true : response.userReaction === 'DISLIKE' ? false : null);
          }
          this.isPostVoteLoading.set(postId, false);
        },
        error => {
          this.errorMessage = error.error?.message || 'Error toggling like';
          this.isPostVoteLoading.set(postId, false);
        }
      );
  }
  
  dislikePost(event: MouseEvent): void {
    console.log('dislikePost called for post:', this.post ? this.post.id : 'unknown');
    event.preventDefault();
    event.stopPropagation();
    if (!this.post || this.isPostVoteLoading.get(this.post.id)) return;
    if (this.isPostAuthor) return;
    const postId = this.post ? this.post.id : null;
    if (postId === null) return;
    this.isPostVoteLoading.set(postId, true);
    const currentReaction = this.userPostVotes.get(postId) === true ? 'LIKE' : this.userPostVotes.get(postId) === false ? 'DISLIKE' : null;
    this.reactionService.toggleDislike(postId, 'POST', currentReaction)
      .subscribe(
        response => {
          if (this.post) {
            this.post.voteCount = response.totalScore;
            this.userPostVotes.set(postId, response.userReaction === 'LIKE' ? true : response.userReaction === 'DISLIKE' ? false : null);
          }
          this.isPostVoteLoading.set(postId, false);
        },
        error => {
          this.errorMessage = error.error?.message || 'Error toggling dislike';
          this.isPostVoteLoading.set(postId, false);
        }
      );
  }
  
  likeComment(commentId: number, event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();

    console.log('LIKE COMMENT button clicked for comment:', commentId);

    const comment = this.findCommentById(this.comments, commentId);
    if (!comment || this.isCommentVoteLoading.get(commentId)) {
      return;
    }

    if (comment.authorId === this.currentUser?.id) {
      console.error('Author cannot like their own comment');
      return;
    }

    this.isCommentVoteLoading.set(commentId, true);

    const currentReaction = this.userCommentVotes.get(commentId) === true ? 'LIKE' :
                           this.userCommentVotes.get(commentId) === false ? 'DISLIKE' : null;

    this.reactionService.toggleLike(commentId, 'COMMENT', currentReaction)
      .subscribe(
        response => {
          console.log('Comment like toggled successfully:', response);
          if (comment) {
            comment.voteCount = response.totalScore;
            this.userCommentVotes.set(
              commentId,
              response.userReaction === 'LIKE' ? true :
              response.userReaction === 'DISLIKE' ? false : null
            );
            this.comments.sort((a, b) => b.voteCount - a.voteCount);
          }
          this.isCommentVoteLoading.set(commentId, false);
        },
        error => {
          console.error('Error toggling comment like:', error);
          this.errorMessage = error.error?.message || 'Error liking comment';
          this.isCommentVoteLoading.set(commentId, false);
        }
      );
  }

  dislikeComment(commentId: number, event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();

    console.log('DISLIKE COMMENT button clicked for comment:', commentId);

    const comment = this.findCommentById(this.comments, commentId);
    if (!comment || this.isCommentVoteLoading.get(commentId)) {
      return;
    }

    if (comment.authorId === this.currentUser?.id) {
      console.error('Author cannot dislike their own comment');
      return;
    }

    this.isCommentVoteLoading.set(commentId, true);

    const currentReaction = this.userCommentVotes.get(commentId) === true ? 'LIKE' :
                           this.userCommentVotes.get(commentId) === false ? 'DISLIKE' : null;

    this.reactionService.toggleDislike(commentId, 'COMMENT', currentReaction)
      .subscribe(
        response => {
          console.log('Comment dislike toggled successfully:', response);
          if (comment) {
            comment.voteCount = response.totalScore;
            this.userCommentVotes.set(
              commentId,
              response.userReaction === 'LIKE' ? true :
              response.userReaction === 'DISLIKE' ? false : null
            );
            this.comments.sort((a, b) => b.voteCount - a.voteCount);
          }
          this.isCommentVoteLoading.set(commentId, false);
        },
        error => {
          console.error('Error toggling comment dislike:', error);
          this.errorMessage = error.error?.message || 'Error disliking comment';
          this.isCommentVoteLoading.set(commentId, false);
        }
      );
  }

  isCommentVoting(commentId: number): boolean {
    return !!this.isCommentVoteLoading.get(commentId);
  }

  deletePost(): void {
    if (!this.post) return;
    
    if (confirm('Are you sure you want to delete this post?')) {
      this.postService.deletePost(this.post.id).subscribe(
        () => {
          this.router.navigate(['/']);
        },
        error => {
          this.errorMessage = error.error?.message || 'Error deleting post';
        }
      );
    }
  }

  deleteComment(commentId: number): void {
    if (confirm('Are you sure you want to delete this comment?')) {
      this.commentService.deleteComment(commentId).subscribe(
        () => {
          this.comments = this.removeCommentAndItsReplies(this.comments, commentId);
        },
        error => {
          this.errorMessage = error.error?.message || 'Error deleting comment';
        }
      );
    }
  }

  updatePostStatus(status: PostStatus): void {
    if (!this.post) return;
    
    this.postService.updatePostStatus(this.post.id, status).subscribe(
      updatedPost => {
        this.post = updatedPost;
      },
      error => {
        this.errorMessage = error.error?.message || 'Error updating post status';
      }
    );
  }

  isCommentAuthor(comment: Comment): boolean {
    return this.currentUser && this.currentUser.id === comment.authorId;
  }

  toggleCommentForm(): void {
    this.isCommentFormVisible = !this.isCommentFormVisible;
    if (this.isCommentFormVisible) {
      this.commentForm.reset();
      this.resetCommentUploadState();
      if (this.activeReplyParentComment) {
        this.activeReplyParentComment.isReplyFormVisible = false;
      }
    } else {
      this.commentForm.reset();
      this.resetCommentUploadState();
    }
  }

  onReplyFileSelected(event: Event): void {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList && fileList.length > 0) {
      this.selectedReplyFile = fileList[0];
      this.replyFileUploadError = null;
      this.replyUploadProgress = 0;
      this.replyForm.controls['imageUrl'].setValue('');
      this.replyPreviewUrl = null;

      const reader = new FileReader();
      reader.onload = (e) => this.replyPreviewUrl = reader.result;
      reader.readAsDataURL(this.selectedReplyFile);

      this.uploadReplyFile();
    } else {
      this.selectedReplyFile = null;
      this.replyPreviewUrl = null;
    }
  }

  uploadReplyFile(): void {
    if (!this.selectedReplyFile) {
      return;
    }

    const formData = new FormData();
    formData.append('file', this.selectedReplyFile, this.selectedReplyFile.name);

    this.replyUploadProgress = 0;
    this.isUploadingReplyImage = true;
    this.replyFileUploadError = null;

    const token = this.tokenStorage.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', 'Bearer ' + token);
    }

    this.http.post(`${this.commentApiUrl}/comments/upload-image`, formData, {
      headers: headers,
      reportProgress: true,
      observe: 'events'
    }).subscribe({
      next: (event: any) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.replyUploadProgress = Math.round(100 * event.loaded / event.total);
        } else if (event.type === HttpEventType.Response && event.body) {
          const responseBody = event.body as { imageUrl: string };
          this.replyForm.controls['imageUrl'].setValue(responseBody.imageUrl);
          this.replyPreviewUrl = this.commentServiceHostForImages + responseBody.imageUrl;
          this.selectedReplyFile = null;
          this.isUploadingReplyImage = false;
          this.replyFileUploadError = null;
        }
      },
      error: (err) => {
        this.replyFileUploadError = err.error?.message || 'Reply image upload failed.';
        console.error("Reply file upload error:", err);
        this.replyUploadProgress = 0;
        this.isUploadingReplyImage = false;
        this.selectedReplyFile = null;
      }
    });
  }

  private resetReplyUploadState(): void {
    this.selectedReplyFile = null;
    this.replyUploadProgress = 0;
    this.replyFileUploadError = null;
    this.replyPreviewUrl = null;
    this.isUploadingReplyImage = false;
  }
  
  private resetCommentUploadState(): void {
    this.selectedCommentFile = null;
    this.commentUploadProgress = 0;
    this.commentFileUploadError = null;
    this.commentPreviewUrl = null;
    this.isUploadingCommentImage = false;
  }

  scrollToComment(commentId: number | undefined): void {
    if (commentId === undefined) return;
    const element = document.getElementById(`comment-${commentId}`);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
      element.classList.add('highlighted-comment');
      setTimeout(() => {
        element.classList.remove('highlighted-comment');
      }, 2000);
    }
  }

  editComment(comment: Comment): void {
    this.router.navigate(['/comments/edit', comment.id]);
  }

  private removeCommentAndItsReplies(comments: Comment[], commentIdToDelete: number): Comment[] {
    return comments.reduce((acc, comment) => {
      if (comment.id === commentIdToDelete) {
        return acc;
      }
      if (comment.replies) {
        comment.replies = this.removeCommentAndItsReplies(comment.replies, commentIdToDelete);
      }
      acc.push(comment);
      return acc;
    }, [] as Comment[]);
  }

  openImageModal(imageUrl: string): void {
    console.log('Image clicked:', imageUrl);
    window.open(imageUrl, '_blank');
  }
} 