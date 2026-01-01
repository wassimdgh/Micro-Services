import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface NotificationMessage {
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notifications$ = new BehaviorSubject<NotificationMessage | null>(null);

  getNotifications(): Observable<NotificationMessage | null> {
    return this.notifications$.asObservable();
  }

  success(message: string, duration = 5000): void {
    this.notify({ message, type: 'success', duration });
  }

  error(message: string, duration = 5000): void {
    this.notify({ message, type: 'error', duration });
  }

  warning(message: string, duration = 5000): void {
    this.notify({ message, type: 'warning', duration });
  }

  info(message: string, duration = 5000): void {
    this.notify({ message, type: 'info', duration });
  }

  private notify(notification: NotificationMessage): void {
    this.notifications$.next(notification);
    if (notification.duration) {
      setTimeout(() => {
        this.notifications$.next(null);
      }, notification.duration);
    }
  }
}
