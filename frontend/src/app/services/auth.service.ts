import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  roles?: string;
}

export interface AuthResponse {
  message: string;
}

export interface AuthUser {
  username: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/auth';
  private currentUserSubject = new BehaviorSubject<AuthUser | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) { }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          const user: AuthUser = { username: request.username };
          localStorage.setItem('currentUser', JSON.stringify(user));
          localStorage.setItem('authToken', btoa(`${request.username}:${request.password}`));
          this.currentUserSubject.next(user);
        })
      );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request);
  }

  changePassword(currentPassword: string, newPassword: string, confirmPassword: string): Observable<AuthResponse> {
    const request = { currentPassword, newPassword, confirmPassword };
    const token = this.getAuthToken();
    
    if (!token) {
      console.error('❌ No auth token found! Please logout and login again.');
      throw new Error('Not authenticated');
    }
    
    const headers = {
      'Authorization': `Basic ${token}`,
      'Content-Type': 'application/json'
    };
    
    console.log('🔐 Sending change password request with auth header');
    const backendUrl = 'http://localhost:8085/api/auth/change-password';
    return this.http.post<AuthResponse>(backendUrl, request, { headers });
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authToken');
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!this.getUserFromStorage();
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  getAuthToken(): string | null {
    const token = localStorage.getItem('authToken');
    console.log('📦 getAuthToken() called, returning:', token ? `${token.substring(0, 10)}...` : 'null');
    return token;
  }

  private getUserFromStorage(): AuthUser | null {
    const user = localStorage.getItem('currentUser');
    return user ? JSON.parse(user) : null;
  }
}
