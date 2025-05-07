import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../../auth/models/user.model';
import { TokenStorageService } from '../../auth/services/token-storage.service';

const API_URL = environment.userApiUrl;

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private http: HttpClient, private tokenStorage: TokenStorageService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.tokenStorage.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', 'Bearer ' + token);
    }
    return headers;
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${API_URL}/users`, { headers: this.getAuthHeaders() });
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${API_URL}/users/${id}`, { headers: this.getAuthHeaders() });
  }

  getUserByUsername(username: string): Observable<User> {
    return this.http.get<User>(`${API_URL}/users/username/${username}`, { headers: this.getAuthHeaders() });
  }

  updateUserScore(id: number, scoreChange: number): Observable<any> {
    return this.http.post(`${API_URL}/users/${id}/score?scoreChange=${scoreChange}`, {}, { headers: this.getAuthHeaders() });
  }

  promoteSelfToModerator(): Observable<User> {
    return this.http.post<User>(`${API_URL}/users/me/promote-to-moderator`, {}, { headers: this.getAuthHeaders() });
  }

  searchUsers(query: string, page: number, size: number): Observable<any> {
    let params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${API_URL}/users/search`, { headers: this.getAuthHeaders(), params: params });
  }

  banUser(userId: number): Observable<User> {
    return this.http.post<User>(`${API_URL}/users/${userId}/ban`, {}, { headers: this.getAuthHeaders() });
  }

  unbanUser(userId: number): Observable<User> {
    return this.http.post<User>(`${API_URL}/users/${userId}/unban`, {}, { headers: this.getAuthHeaders() });
  }
} 