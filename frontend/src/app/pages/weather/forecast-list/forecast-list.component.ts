import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { WeatherService } from '../../../services/weather.service';
import { NotificationService } from '../../../services/notification.service';

interface Prevision {
  id: number;
  date: string;
  temperatureMax: number;
  temperatureMin: number;
  pluiePrevue: number;
  vent: number;
  station?: any;
}

@Component({
  selector: 'app-forecast-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatFormFieldModule
  ],
  templateUrl: './forecast-list.component.html',
  styleUrls: ['./forecast-list.component.scss']
})
export class ForecastListComponent implements OnInit {
  forecasts: Prevision[] = [];
  allForecasts: Prevision[] = [];
  stations: any[] = [];
  selectedStationId: number | null = null;
  loading = true;
  displayedColumns: string[] = ['id', 'date', 'tempMax', 'tempMin', 'rain', 'wind', 'station'];

  constructor(
    private weatherService: WeatherService,
    private notificationService: NotificationService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadStations();
    this.loadForecasts();
  }

  loadStations(): void {
    this.weatherService.getAllStations().subscribe({
      next: (data) => {
        this.stations = data;
      },
      error: (error) => {
        console.error('Error loading stations:', error);
      }
    });
  }

  loadForecasts(): void {
    this.loading = true;
    this.weatherService.getAllPrevisions().subscribe({
      next: (data) => {
        this.allForecasts = data;
        this.filterForecasts();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading forecasts:', error);
        this.notificationService.warning('No forecasts available yet. Auto-generation starts 1 minute after creating a station.');
        this.loading = false;
      }
    });
  }

  filterForecasts(): void {
    if (this.selectedStationId) {
      this.forecasts = this.allForecasts.filter(f => f.station?.id === this.selectedStationId);
    } else {
      this.forecasts = this.allForecasts;
    }
  }

  onStationFilterChange(stationId: number | null): void {
    this.selectedStationId = stationId;
    this.filterForecasts();
  }

  getStationName(forecast: Prevision): string {
    return forecast.station?.nom || 'Station ' + (forecast.station?.id || '-');
  }
}
