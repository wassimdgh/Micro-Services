import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { WeatherService, StationMeteo } from '../../../services/weather.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-station-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './station-form.component.html',
  styleUrls: ['./station-form.component.scss']
})
export class StationFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  submitting = false;
  isEditMode = false;
  stationId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private weatherService: WeatherService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        const parsedId = Number(params['id']);
        if (!Number.isNaN(parsedId)) {
          this.stationId = parsedId;
          this.loadStation(parsedId);
        }
      }
    });
  }

  initializeForm(): void {
    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(3)]],
      latitude: ['', [Validators.required, Validators.pattern(/^-?[0-9]{1,2}\.[0-9]+$/)]],
      longitude: ['', [Validators.required, Validators.pattern(/^-?[0-9]{1,3}\.[0-9]+$/)]],
      fournisseur: ['', [Validators.required, Validators.minLength(2)]]
    });
  }

  loadStation(id: number): void {
    this.loading = true;
    this.weatherService.getStation(id).subscribe({
      next: (station) => {
        this.form.patchValue(station);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading station:', error);
        this.notificationService.error('Failed to load station');
        this.loading = false;
        this.router.navigate(['/weather/stations']);
      }
    });
  }

  submit(): void {
    if (!this.form.valid) {
      this.notificationService.warning('Please fill in all required fields correctly');
      return;
    }

    this.submitting = true;
    const formData: StationMeteo = this.form.value;

    const request = this.isEditMode
      ? this.weatherService.updateStation(this.stationId!, formData)
      : this.weatherService.createStation(formData);

    request.subscribe({
      next: (result) => {
        this.submitting = false;
        const message = this.isEditMode ? 'Station updated successfully' : 'Station created successfully';
        this.notificationService.success(message);
        this.router.navigate(['/weather/stations']);
      },
      error: (error) => {
        console.error('Error saving station:', error);
        this.submitting = false;
        this.notificationService.error('Failed to save station');
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/weather/stations']);
  }

  get nom() { return this.form.get('nom'); }
  get latitude() { return this.form.get('latitude'); }
  get longitude() { return this.form.get('longitude'); }
  get fournisseur() { return this.form.get('fournisseur'); }
}
