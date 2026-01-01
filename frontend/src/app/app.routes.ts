import { Routes } from '@angular/router';
import { AuthGuard } from './services/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/auth/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/auth/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'settings',
    canActivate: [AuthGuard],
    loadComponent: () => import('./pages/settings/settings.component').then(m => m.SettingsComponent)
  },
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'weather',
    canActivate: [AuthGuard],
    children: [
      {
        path: 'stations',
        loadComponent: () => import('./pages/weather/station-list/station-list.component').then(m => m.StationListComponent)
      },
      {
        path: 'stations/new',
        loadComponent: () => import('./pages/weather/station-form/station-form.component').then(m => m.StationFormComponent)
      },
      {
        path: 'stations/:id/edit',
        loadComponent: () => import('./pages/weather/station-form/station-form.component').then(m => m.StationFormComponent)
      },
      {
        path: 'forecasts',
        loadComponent: () => import('./pages/weather/forecast-list/forecast-list.component').then(m => m.ForecastListComponent)
      },
      {
        path: 'forecasts/new',
        loadComponent: () => import('./pages/weather/forecast-form/forecast-form.component').then(m => m.ForecastFormComponent)
      }
    ]
  },
  {
    path: 'irrigation',
    canActivate: [AuthGuard],
    children: [
      {
        path: 'schedules',
        loadComponent: () => import('./pages/irrigation/schedule-list/schedule-list.component').then(m => m.ScheduleListComponent)
      },
      {
        path: 'schedules/new',
        loadComponent: () => import('./pages/irrigation/schedule-form/schedule-form.component').then(m => m.ScheduleFormComponent)
      },
      {
        path: 'schedules/:id/edit',
        loadComponent: () => import('./pages/irrigation/schedule-form/schedule-form.component').then(m => m.ScheduleFormComponent)
      },
      {
        path: 'logs',
        loadComponent: () => import('./pages/irrigation/execution-log-list/execution-log-list.component').then(m => m.ExecutionLogListComponent)
      },
      {
        path: 'logs/new',
        loadComponent: () => import('./pages/irrigation/execution-log-form/execution-log-form.component').then(m => m.ExecutionLogFormComponent)
      }
    ]
  }
];
