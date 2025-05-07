import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Comment, CommentRequest } from '../models/comment.model';

const API_URL = environment.commentApiUrl;

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  constructor(private http: HttpClient) { }

  getCommentsByPostId(postId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${API_URL}/comments/post/${postId}`);
  }

  getPagedCommentsByPostId(postId: number, page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${API_URL}/comments/post/${postId}/paged?page=${page}&size=${size}`);
  }

  getCommentById(id: number): Observable<Comment> {
    return this.http.get<Comment>(`${API_URL}/comments/${id}`);
  }

  createComment(commentRequest: CommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${API_URL}/comments`, commentRequest);
  }

  updateComment(id: number, commentRequest: CommentRequest): Observable<Comment> {
    return this.http.put<Comment>(`${API_URL}/comments/${id}`, commentRequest);
  }

  deleteComment(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/comments/${id}`);
  }

  getCommentsByAuthorId(authorId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${API_URL}/comments/author/${authorId}`);
  }

  voteComment(id: number, upvote: boolean): Observable<Comment> {
    return this.http.post<Comment>(`${API_URL}/comments/${id}/vote?upvote=${upvote}`, {});
  }

  getReplies(commentId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${API_URL}/comments/${commentId}/replies`);
  }
} 