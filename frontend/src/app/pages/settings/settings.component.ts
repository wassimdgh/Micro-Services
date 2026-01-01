import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { AuthService, AuthUser } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatTabsModule,
    MatDividerModule,
    MatIconModule
  ],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  currentUser: AuthUser | null = null;
  passwordForm!: FormGroup;
  loading = false;

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.initializePasswordForm();
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  initializePasswordForm(): void {
    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: any } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }

  changePassword(): void {
    if (this.passwordForm.hasError('passwordMismatch')) {
      this.notificationService.warning('New passwords do not match');
      return;
    }

    if (!this.passwordForm.valid) {
      this.notificationService.warning('Please fill in all password fields correctly');
      return;
    }

    this.loading = true;
    const { currentPassword, newPassword, confirmPassword } = this.passwordForm.value;
    
    this.authService.changePassword(currentPassword, newPassword, confirmPassword).subscribe({
      next: (response) => {
        this.loading = false;
        const successMsg = response.message || 'Password changed successfully!';
        this.notificationService.success(successMsg);
        alert(successMsg); // Show browser alert
        this.passwordForm.reset();
      },
      error: (error) => {
        this.loading = false;
        console.error('Change password error:', error);
        const errorMsg = error?.error?.message || 'Failed to change password';
        this.notificationService.error(errorMsg);
        alert('Error: ' + errorMsg); // Show error alert
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
