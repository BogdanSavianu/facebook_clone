import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { JwtResponse, LoginRequest, SignupRequest } from '../models/user.model';

const AUTH_API = environment.authApiUrl + '/auth';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) { }

  login(loginRequest: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(AUTH_API + '/signin', loginRequest, httpOptions);
  }

  register(signupRequest: SignupRequest): Observable<any> {
    return this.http.post(AUTH_API + '/signup', signupRequest, httpOptions);
  }
} 