import { Component, OnInit } from '@angular/core';
import { PostService, PostFilters } from '../../../post/services/post.service';
import { ReactionService } from '../../../reaction/services/reaction.service';
import { Post, Tag } from '../../../post/models/post.model';
import { TokenStorageService } from '../../../auth/services/token-storage.service';
import { UserService } from '../../../shared/services/user.service';
import { User } from '../../../auth/models/user.model';
import { HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  posts: Post[] = [];
  currentPage = 0;
  totalPages = 0;
  isLoading = false;
  errorMessage = '';
  currentUser: any;
  isModerator = false;
  postServiceBaseUrl: string = environment.postServiceBaseUrl;

  filterTagName: string | undefined = undefined;
  filterTitleSearch: string | undefined = undefined;
  filterAuthorId: number | undefined = undefined;
  filterMyPosts: boolean | undefined = undefined;

  allTags: Tag[] = [];
  suggestedTags: Tag[] = [];
  showTagSuggestions = false;

  suggestedAuthors: any[] = [];
  showAuthorSuggestions = false;
  isSearchingAuthors = false;
  authorSearchQuery = '';

  userPostVotes: Map<number, boolean | null> = new Map();
  isPostVoteLoading: Map<number, boolean> = new Map();

  constructor(
    private postService: PostService,
    private reactionService: ReactionService,
    private tokenStorage: TokenStorageService,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    console.log('HomeComponent initialized');
    this.currentUser = this.tokenStorage.getUser();
    if (this.currentUser && this.currentUser.roles) {
        this.isModerator = this.currentUser.roles.includes('ROLE_MODERATOR');
    }
    this.loadPosts();
    this.loadAllTags();
  }

  loadAllTags(): void {
    this.postService.getAllTags().subscribe({
      next: (tags) => {
        this.allTags = tags;
        console.log('Loaded all tags:', this.allTags);
      },
      error: (err) => {
        console.error('Failed to load tags:', err);
      }
    });
  }

  loadPosts(resetPage: boolean = false): void {
    if (resetPage) {
      this.currentPage = 0;
    }
    this.isLoading = true;

    const filters: PostFilters = {
      tagName: this.filterTagName,
      titleSearch: this.filterTitleSearch,
      authorId: this.filterAuthorId,
      myPosts: this.filterMyPosts
    };

    this.postService.getAllPosts(this.currentPage, 10, filters).subscribe(
      data => {
        this.posts = data.content;
        this.totalPages = data.totalPages;
        this.isLoading = false;
        this.posts.forEach(post => {
          if (post.id) {
          this.reactionService.getReactionStats(post.id, 'POST').subscribe(
            response => {
              this.userPostVotes.set(post.id, response.userReaction === 'LIKE' ? true : response.userReaction === 'DISLIKE' ? false : null);
            },
            error => {
              this.userPostVotes.set(post.id, null);
            }
          );
          }
        });
      },
      (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Error loading posts.';
      }
    );
  }

  likePost(post: Post, event: MouseEvent): void {
    console.log('LIKE button pressed for post:', post.id);
    event.preventDefault();
    event.stopPropagation();
    if (!post || !post.id || this.isPostVoteLoading.get(post.id)) return;
    if (post.authorId === this.currentUser?.id) return;
    this.isPostVoteLoading.set(post.id, true);
    const currentReaction = this.userPostVotes.get(post.id) === true ? 'LIKE' : this.userPostVotes.get(post.id) === false ? 'DISLIKE' : null;
    this.reactionService.toggleLike(post.id, 'POST', currentReaction).subscribe(
      response => {
        post.voteCount = response.totalScore;
        this.userPostVotes.set(post.id, response.userReaction === 'LIKE' ? true : response.userReaction === 'DISLIKE' ? false : null);
        if (response.authorScore !== null && response.authorScore !== undefined) {
          post.authorUserScore = response.authorScore;
        }
        if (response.performingUserId === this.currentUser?.id && response.performingUserScore !== null && response.performingUserScore !== undefined) {
          this.currentUser.userScore = response.performingUserScore;
          this.tokenStorage.saveUser(this.currentUser);
        }
        this.isPostVoteLoading.set(post.id, false);
      },
      (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message || 'Error toggling like';
        this.isPostVoteLoading.set(post.id, false);
      }
    );
  }

  dislikePost(post: Post, event: MouseEvent): void {
    console.log('DISLIKE button pressed for post:', post.id);
    event.preventDefault();
    event.stopPropagation();
    if (!post || !post.id || this.isPostVoteLoading.get(post.id)) return;
    if (post.authorId === this.currentUser?.id) return;
    this.isPostVoteLoading.set(post.id, true);
    const currentReaction = this.userPostVotes.get(post.id) === true ? 'LIKE' : this.userPostVotes.get(post.id) === false ? 'DISLIKE' : null;
    this.reactionService.toggleDislike(post.id, 'POST', currentReaction).subscribe(
      response => {
        post.voteCount = response.totalScore;
        this.userPostVotes.set(post.id, response.userReaction === 'LIKE' ? true : response.userReaction === 'DISLIKE' ? false : null);
        if (response.authorScore !== null && response.authorScore !== undefined) {
          post.authorUserScore = response.authorScore;
        }
        if (response.performingUserId === this.currentUser?.id && response.performingUserScore !== null && response.performingUserScore !== undefined) {
          this.currentUser.userScore = response.performingUserScore;
          this.tokenStorage.saveUser(this.currentUser);
        }
        this.isPostVoteLoading.set(post.id, false);
      },
      (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message || 'Error toggling dislike';
        this.isPostVoteLoading.set(post.id, false);
      }
    );
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadPosts();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadPosts();
    }
  }

  becomeModerator(): void {
    this.userService.promoteSelfToModerator().subscribe({
      next: (updatedUser: User) => {
        console.log('Successfully promoted to moderator:', updatedUser);
        this.tokenStorage.saveUser(updatedUser);
        this.currentUser = updatedUser;
        this.isModerator = this.currentUser.roles?.includes('ROLE_MODERATOR') ?? false;
        alert('You are now a moderator! Your roles have been updated.');
      },
      error: (err: HttpErrorResponse) => {
        console.error('Failed to become moderator:', err);
        alert('Failed to become moderator. Error: ' + (err.error?.message || err.message));
      }
    });
  }

  updateTagSuggestions(inputElement: HTMLInputElement): void {
    const query = inputElement.value.toLowerCase();
    if (query.length > 0) {
      this.suggestedTags = this.allTags.filter(tag => 
        tag.name.toLowerCase().includes(query)
      );
      this.showTagSuggestions = this.suggestedTags.length > 0;
    } else {
      this.suggestedTags = [];
      this.showTagSuggestions = false;
    }
  }

  selectTagSuggestion(tag: Tag): void {
    this.applyTagFilter(tag.name);
    this.showTagSuggestions = false;
    const tagInput = document.querySelector('#tagFilterInput') as HTMLInputElement;
    if (tagInput) tagInput.value = tag.name;
  }

  hideTagSuggestionsWithDelay(): void {
    setTimeout(() => { this.showTagSuggestions = false; }, 200);
  }

  updateAuthorSuggestions(inputElement: HTMLInputElement): void {
    const query = inputElement.value.trim();
    this.authorSearchQuery = query;

    if (query.length < 2) {
      this.suggestedAuthors = [];
      this.showAuthorSuggestions = false;
      return;
    }

    this.isSearchingAuthors = true;
    this.showAuthorSuggestions = true;

    this.userService.searchUsers(query, 0, 5)
      .subscribe({
        next: (response) => {
          if (this.authorSearchQuery === query) { 
             this.suggestedAuthors = response.content; 
          }
          this.isSearchingAuthors = false;
        },
        error: (err) => {
          console.error('Error searching authors:', err);
          this.suggestedAuthors = [];
          this.isSearchingAuthors = false;
        }
      });
  }

  selectAuthorSuggestion(author: any): void {
    if (author && author.id) {
      this.filterByAuthor(author.id); 
      this.showAuthorSuggestions = false;
      const authorInput = document.querySelector('#authorFilterInput') as HTMLInputElement;
      if (authorInput) authorInput.value = author.username; 
    } else {
        console.error('Selected author suggestion is invalid:', author);
    }
  }

  hideAuthorSuggestionsWithDelay(): void {
    setTimeout(() => { this.showAuthorSuggestions = false; }, 200);
  }

  applyTitleSearch(title: string): void {
    this.filterTitleSearch = title ? title.trim() : undefined;
    this.loadPosts(true);
  }

  applyTagFilter(tagName: string): void {
    this.filterTagName = tagName ? tagName.trim() : undefined;
    this.showTagSuggestions = false;
    this.loadPosts(true);
  }

  showMyPosts(): void {
    this.filterMyPosts = true;
    this.filterAuthorId = undefined; 
    this.filterTagName = undefined;
    this.filterTitleSearch = undefined;
    this.showAuthorSuggestions = false;
    this.loadPosts(true);
    const authorInput = document.querySelector('#authorFilterInput') as HTMLInputElement;
    if(authorInput) authorInput.value = '';
  }

  showAllPosts(): void {
    this.filterMyPosts = undefined;
    this.filterTagName = undefined;
    this.filterTitleSearch = undefined;
    this.filterAuthorId = undefined;
    const titleInput = document.querySelector('#titleSearchInput') as HTMLInputElement;
    if(titleInput) titleInput.value = '';
    const tagInput = document.querySelector('#tagFilterInput') as HTMLInputElement;
    if(tagInput) tagInput.value = '';
    const authorInput = document.querySelector('#authorFilterInput') as HTMLInputElement;
    if(authorInput) authorInput.value = '';

    this.loadPosts(true);
  }

  filterByAuthor(authorId: number): void {
    this.filterAuthorId = authorId;
    this.filterMyPosts = undefined; 
    this.filterTagName = undefined;
    this.filterTitleSearch = undefined;
    this.showAuthorSuggestions = false;
    this.loadPosts(true);
  }
} 