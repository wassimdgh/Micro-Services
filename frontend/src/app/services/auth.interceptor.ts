import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getAuthToken();
    console.log('🔐 Interceptor - Token from service:', token ? 'EXISTS' : 'MISSING');
    if (token) {
      const authHeader = `Basic ${token}`;
      console.log('🔐 Adding Authorization header:', authHeader.substring(0, 20) + '...');
      req = req.clone({
        setHeaders: {
          Authorization: authHeader
        }
      });
    } else {
      console.log('⚠️ No token found in localStorage');
    }
    return next.handle(req);
  }
}
