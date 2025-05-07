import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Post, PostRequest, Tag } from '../models/post.model';

const API_URL = environment.postApiUrl;

export interface PostFilters {
  tagName?: string;
  titleSearch?: string;
  authorId?: number;
  myPosts?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class PostService {
  constructor(private http: HttpClient) { }

  getAllPosts(page: number = 0, size: number = 10, filters?: PostFilters): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filters) {
      if (filters.tagName) {
        params = params.append('tagName', filters.tagName);
      }
      if (filters.titleSearch) {
        params = params.append('titleSearch', filters.titleSearch);
      }
      if (filters.authorId !== null && filters.authorId !== undefined) {
        params = params.append('authorId', filters.authorId.toString());
      }
      if (filters.myPosts !== null && filters.myPosts !== undefined) {
        params = params.append('myPosts', filters.myPosts.toString());
      }
    }

    return this.http.get(`${API_URL}/posts`, { params });
  }

  getPostById(id: number): Observable<Post> {
    return this.http.get<Post>(`${API_URL}/posts/${id}`);
  }

  createPost(postRequest: PostRequest): Observable<Post> {
    return this.http.post<Post>(`${API_URL}/posts`, postRequest);
  }

  updatePost(id: number, postRequest: PostRequest): Observable<Post> {
    return this.http.put<Post>(`${API_URL}/posts/${id}`, postRequest);
  }

  deletePost(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/posts/${id}`);
  }

  getPostsByAuthor(authorId: number): Observable<Post[]> {
    return this.http.get<Post[]>(`${API_URL}/posts/author/${authorId}`);
  }

  searchPosts(query: string, page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${API_URL}/posts/search?query=${query}&page=${page}&size=${size}`);
  }

  getPostsByTag(tagName: string, page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${API_URL}/posts/tag/${tagName}?page=${page}&size=${size}`);
  }

  updatePostStatus(id: number, status: string): Observable<Post> {
    return this.http.put<Post>(`${API_URL}/posts/${id}/status?status=${status}`, {});
  }

  votePost(id: number, upvote: boolean): Observable<Post> {
    return this.http.post<Post>(`${API_URL}/posts/${id}/vote?upvote=${upvote}`, {});
  }

  getAllTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(`${API_URL}/tags`);
  }

  createTag(name: string): Observable<Tag> {
    return this.http.post<Tag>(`${API_URL}/tags?name=${name}`, {});
  }
} 