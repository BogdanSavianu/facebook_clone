import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TokenStorageService } from '../../auth/services/token-storage.service';

const API_URL = environment.reactionApiUrl;

@Injectable({
  providedIn: 'root'
})
export class ReactionService {
  
  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService
  ) { }
  
  getApiUrl(): string {
    return API_URL;
  }
  
  addReaction(entityId: number, entityType: string, isLike: boolean): Observable<any> {
    const token = this.tokenStorage.getToken();
    
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
    
    const payload = {
      entityId,
      entityType: entityType.toUpperCase(),
      isLike
    };
    
    return this.http.post(`${API_URL}/reactions`, payload, { headers });
  }
  
  removeReaction(entityId: number, entityType: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${this.tokenStorage.getToken()}`
    });
    
    return this.http.delete(`${API_URL}/reactions/${entityType.toUpperCase()}/${entityId}`, { headers });
  }
  
  getReactionStats(entityId: number, entityType: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${this.tokenStorage.getToken()}`
    });
    
    return this.http.get(`${API_URL}/reactions/${entityType.toUpperCase()}/${entityId}`, { headers });
  }
  
  toggleLike(entityId: number, entityType: string, currentReaction: string | null): Observable<any> {
    if (currentReaction === 'LIKE') {
      return this.removeReaction(entityId, entityType);
    } else {
      return this.addReaction(entityId, entityType, true);
    }
  }
  
  toggleDislike(entityId: number, entityType: string, currentReaction: string | null): Observable<any> {
    if (currentReaction === 'DISLIKE') {
      return this.removeReaction(entityId, entityType);
    } else {
      return this.addReaction(entityId, entityType, false);
    }
  }
} 