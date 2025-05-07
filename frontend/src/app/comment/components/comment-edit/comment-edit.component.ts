import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommentService } from '../../services/comment.service';
import { TokenStorageService } from '../../../auth/services/token-storage.service';
import { HttpClient, HttpEventType, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { CommentRequest } from '../../models/comment.model';

@Component({
  selector: 'app-comment-edit',
  templateUrl: './comment-edit.component.html',
  styleUrls: ['./comment-edit.component.scss']
})
export class CommentEditComponent implements OnInit {
  commentForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  commentId!: number;
  postId?: number;
  currentUser: any;
  commentServiceHostForImages: string;

  selectedFile: File | null = null;
  uploadProgress = 0;
  fileUploadError: string | null = null;
  previewUrl: string | ArrayBuffer | null = null;
  isUploadingImage = false;

  constructor(
    private formBuilder: FormBuilder,
    private commentService: CommentService,
    private route: ActivatedRoute,
    private router: Router,
    private tokenStorage: TokenStorageService,
    private http: HttpClient
  ) {
    const apiUrl = environment.commentApiUrl;
    if (apiUrl.endsWith('/api')) {
      this.commentServiceHostForImages = apiUrl.slice(0, -4);
    } else {
      this.commentServiceHostForImages = apiUrl;
    }
  }

  ngOnInit(): void {
    this.currentUser = this.tokenStorage.getUser();
    
    this.commentForm = this.formBuilder.group({
      content: ['', [Validators.required, Validators.maxLength(500)]],
      imageUrl: ['']
    });

    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.commentId = +id;
        this.loadComment(this.commentId);
      } else {
        this.errorMessage = "No comment ID provided for editing.";
        this.isLoading = false;
      }
    });
  }

  loadComment(id: number): void {
    this.isLoading = true;
    this.commentService.getCommentById(id).subscribe(
      comment => {
        if (comment.authorId !== this.currentUser.id && !this.currentUser.roles.includes('ROLE_MODERATOR')) {
          this.errorMessage = 'You are not authorized to edit this comment';
          this.router.navigate(['/']);
          this.isLoading = false;
          return;
        }
        
        this.commentForm.patchValue({
          content: comment.content,
          imageUrl: comment.imageUrl || ''
        });
        this.postId = comment.postId;
        this.isLoading = false;
      },
      error => {
        this.errorMessage = error.error?.message || 'Error loading comment';
        this.isLoading = false;
      }
    );
  }

  onFileSelected(event: Event): void {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList && fileList.length > 0) {
      this.selectedFile = fileList[0];
      this.fileUploadError = null;
      this.uploadProgress = 0;
      this.commentForm.controls['imageUrl'].setValue('');
      this.previewUrl = null;

      const reader = new FileReader();
      reader.onload = (e) => this.previewUrl = reader.result;
      reader.readAsDataURL(this.selectedFile);

      this.uploadFile();
    } else {
      this.selectedFile = null;
      this.previewUrl = null;
    }
  }

  uploadFile(): void {
    if (!this.selectedFile) {
      return;
    }
    const formData = new FormData();
    formData.append('file', this.selectedFile, this.selectedFile.name);

    this.uploadProgress = 0;
    this.isUploadingImage = true;
    this.fileUploadError = null;

    const token = this.tokenStorage.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', 'Bearer ' + token);
    }

    this.http.post(`${environment.commentApiUrl}/comments/upload-image`, formData, {
      headers: headers,
      reportProgress: true,
      observe: 'events'
    }).subscribe({
      next: (event: any) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.uploadProgress = Math.round(100 * event.loaded / event.total);
        } else if (event.type === HttpEventType.Response && event.body) {
          const responseBody = event.body as { imageUrl: string };
          this.commentForm.controls['imageUrl'].setValue(responseBody.imageUrl);
          this.selectedFile = null;
          this.isUploadingImage = false;
        }
      },
      error: (err) => {
        this.fileUploadError = err.error?.message || 'File upload failed.';
        this.uploadProgress = 0;
        this.isUploadingImage = false;
        this.selectedFile = null;
      }
    });
  }

  onSubmit(): void {
    if (this.commentForm.invalid) {
      return;
    }

    if (this.isUploadingImage) {
      this.fileUploadError = 'Image is still uploading. Please wait.';
      return;
    }
    if (this.selectedFile && !this.commentForm.controls['imageUrl'].value) {
        this.fileUploadError = 'Image upload may have failed. Please re-select or remove the file.';
        return; 
    }

    if (this.postId === undefined) {
        this.errorMessage = "Post ID is missing. Cannot update comment.";
        this.isLoading = false;
        return;
    }

    this.isLoading = true;

    const commentRequest: CommentRequest = {
      postId: this.postId,
      content: this.commentForm.value.content,
      imageUrl: this.commentForm.value.imageUrl || null
    };

    this.commentService.updateComment(this.commentId, commentRequest)
      .subscribe(
        updatedComment => {
          this.router.navigate(['/posts', updatedComment.postId]);
        },
        error => {
          this.errorMessage = error.error?.message || 'Error updating comment';
          this.isLoading = false;
        }
      );
  }

  cancel(): void {
    if (this.postId) {
      this.router.navigate(['/posts', this.postId]);
    } else {
      this.router.navigate(['/']);
    }
  }
} 