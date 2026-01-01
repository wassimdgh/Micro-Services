import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  form!: FormGroup;
  loading = false;
  roles = ['ROLE_USER', 'ROLE_ADMIN'];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.initializeForm();
  }

  initializeForm(): void {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(64)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(128)]],
      confirmPassword: ['', [Validators.required]],
      roles: ['ROLE_USER', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: any } | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  submit(): void {
    console.log('Register submit clicked. Form valid:', this.form.valid);
    console.log('Form value:', this.form.value);
    console.log('Form errors:', this.form.errors);

    if (this.form.hasError('passwordMismatch')) {
      this.notificationService.warning('Passwords do not match');
      return;
    }

    if (!this.form.valid) {
      this.notificationService.warning('Please fill in all fields correctly');
      console.log('Form validation errors:', this.form.errors);
      return;
    }

    this.loading = true;
    const { username, password, roles } = this.form.value;
    console.log('Submitting register with:', { username, password: '***', roles });

    this.authService.register({ username, password, roles }).subscribe({
      next: (response) => {
        this.loading = false;
        console.log('Registration success:', response);
        this.notificationService.success('Registration successful! You can now login.');
        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.loading = false;
        console.error('Registration error:', error);
        const errorMsg = error?.error?.message || 'Registration failed. Username may already exist.';
        this.notificationService.error(errorMsg);
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
