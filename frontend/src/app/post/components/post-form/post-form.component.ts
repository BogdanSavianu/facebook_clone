import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PostService } from '../../services/post.service';
import { Post, PostRequest } from '../../models/post.model';
import { TokenStorageService } from '../../../auth/services/token-storage.service';
import { HttpClient, HttpEventType, HttpResponse, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-post-form',
  templateUrl: './post-form.component.html',
  styleUrls: ['./post-form.component.scss']
})
export class PostFormComponent implements OnInit {
  postForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  isEditMode = false;
  postId?: number;
  currentUser: any;
  availableTags: string[] = [];
  selectedTags: string[] = [];
  newTag = '';

  selectedFile: File | null = null;
  uploadProgress = 0;
  fileUploadError: string | null = null;
  previewUrl: string | ArrayBuffer | null = null;
  private postApiUrl = environment.postApiUrl;

  constructor(
    private formBuilder: FormBuilder,
    private postService: PostService,
    private route: ActivatedRoute,
    private router: Router,
    private tokenStorage: TokenStorageService,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    this.currentUser = this.tokenStorage.getUser();
    
    this.postForm = this.formBuilder.group({
      title: ['', [Validators.required, Validators.maxLength(255)]],
      content: ['', [Validators.required]],
      imageUrl: ['']
    });

    this.loadTags();

    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.postId = +id;
        this.loadPost(this.postId);
      }
    });
  }

  loadTags(): void {
    this.postService.getAllTags().subscribe(
      tags => {
        this.availableTags = tags.map(tag => tag.name);
      },
      error => {
        console.error('Error loading tags', error);
      }
    );
  }

  loadPost(id: number): void {
    this.isLoading = true;
    this.postService.getPostById(id).subscribe(
      post => {
        if (post.authorId !== this.currentUser.id && !(this.currentUser.roles && this.currentUser.roles.includes('ROLE_MODERATOR'))) {
          this.errorMessage = 'You are not authorized to edit this post';
          this.router.navigate(['/']);
          return;
        }
        this.postForm.patchValue({
          title: post.title,
          content: post.content,
          imageUrl: post.imageUrl || ''
        });
        this.selectedTags = post.tags.map(tag => tag.name);
        this.isLoading = false;
      },
      error => {
        this.errorMessage = error.error?.message || 'Error loading post';
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
      this.postForm.controls['imageUrl'].setValue('');
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
    this.isLoading = true;

    const token = this.tokenStorage.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', 'Bearer ' + token);
    }

    this.http.post(`${this.postApiUrl}/posts/upload-image`, formData, {
      headers: headers,
      reportProgress: true,
      observe: 'events'
    }).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.uploadProgress = Math.round(100 * event.loaded / event.total);
        } else if (event.type === HttpEventType.Response && event.body) {
          const responseBody = event.body as { imageUrl: string };
          this.postForm.controls['imageUrl'].setValue(responseBody.imageUrl);
          this.previewUrl = responseBody.imageUrl;
          this.selectedFile = null;
          this.isLoading = false;
          this.fileUploadError = null;
        }
      },
      error: (err) => {
        this.fileUploadError = err.error?.message || 'File upload failed. Please try again.';
        console.error("File upload error:", err);
        this.uploadProgress = 0;
        this.isLoading = false;
        this.selectedFile = null;
      }
    });
  }

  onSubmit(): void {
    if (this.postForm.invalid) {
      return;
    }

    if (this.selectedFile && !this.postForm.controls['imageUrl'].value) {
        this.fileUploadError = 'Image is still uploading or failed. Please wait or try re-uploading.';
        return;
    }

    const postRequest: PostRequest = {
      title: this.postForm.value.title,
      content: this.postForm.value.content,
      imageUrl: this.postForm.value.imageUrl,
      tagNames: this.selectedTags
    };

    this.isLoading = true;

    if (this.isEditMode && this.postId) {
      this.postService.updatePost(this.postId, postRequest).subscribe(
        post => {
          this.router.navigate(['/posts', post.id]);
        },
        error => {
          this.errorMessage = error.error?.message || 'Error updating post';
          this.isLoading = false;
        }
      );
    } else {
      this.postService.createPost(postRequest).subscribe(
        post => {
          this.router.navigate(['/posts', post.id]);
        },
        error => {
          this.errorMessage = error.error?.message || 'Error creating post';
          this.isLoading = false;
        }
      );
    }
  }

  toggleTag(tag: string): void {
    const index = this.selectedTags.indexOf(tag);
    if (index === -1) {
      this.selectedTags.push(tag);
    } else {
      this.selectedTags.splice(index, 1);
    }
  }

  addNewTag(): void {
    if (!this.newTag.trim()) {
      return;
    }
    
    if (this.availableTags.includes(this.newTag)) {
      if (!this.selectedTags.includes(this.newTag)) {
        this.selectedTags.push(this.newTag);
      }
      this.newTag = '';
      return;
    }
    
    this.postService.createTag(this.newTag).subscribe(
      tag => {
        this.availableTags.push(tag.name);
        this.selectedTags.push(tag.name);
        this.newTag = '';
      },
      error => {
        this.errorMessage = error.error?.message || 'Error creating tag';
      }
    );
  }
} 