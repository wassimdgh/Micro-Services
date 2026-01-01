import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { WeatherService, StationMeteo } from '../../../services/weather.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-station-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './station-list.component.html',
  styleUrls: ['./station-list.component.scss']
})
export class StationListComponent implements OnInit {
  stations: StationMeteo[] = [];
  loading = true;
  displayedColumns: string[] = ['id', 'nom', 'latitude', 'longitude', 'fournisseur', 'actions'];

  constructor(
    private weatherService: WeatherService,
    private notificationService: NotificationService,
    private router: Router,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.loadStations();
  }

  loadStations(): void {
    this.loading = true;
    this.weatherService.getAllStations().subscribe({
      next: (data) => {
        this.stations = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading stations:', error);
        this.notificationService.error('Failed to load stations');
        this.loading = false;
      }
    });
  }

  createNew(): void {
    this.router.navigate(['/weather/stations/new']);
  }

  edit(station: StationMeteo): void {
    this.router.navigate(['/weather/stations', station.id, 'edit']);
  }

  delete(station: StationMeteo): void {
    if (confirm(`Are you sure you want to delete station "${station.nom}"?`)) {
      this.weatherService.deleteStation(station.id!).subscribe({
        next: () => {
          this.notificationService.success('Station deleted successfully');
          this.loadStations();
        },
        error: (error) => {
          console.error('Error deleting station:', error);
          this.notificationService.error('Failed to delete station');
        }
      });
    }
  }
}
