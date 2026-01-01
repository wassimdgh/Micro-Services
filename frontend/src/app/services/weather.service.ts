import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface StationMeteo {
  id?: number;
  nom: string;
  latitude: number;
  longitude: number;
  fournisseur: string;
}

@Injectable({
  providedIn: 'root'
})
export class WeatherService {
  private apiUrl = '/api/meteo';

  constructor(private http: HttpClient) { }

  // Station Methods
  getAllStations(): Observable<StationMeteo[]> {
    return this.http.get<StationMeteo[]>(`${this.apiUrl}/stations`);
  }

  getStation(id: number): Observable<StationMeteo> {
    return this.http.get<StationMeteo>(`${this.apiUrl}/stations/${id}`);
  }

  createStation(station: StationMeteo): Observable<StationMeteo> {
    return this.http.post<StationMeteo>(`${this.apiUrl}/stations`, station);
  }

  updateStation(id: number, station: StationMeteo): Observable<StationMeteo> {
    return this.http.put<StationMeteo>(`${this.apiUrl}/stations/${id}`, station);
  }

  deleteStation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/stations/${id}`);
  }

  // Forecast Methods
  getPrevisions(stationId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/previsions/${stationId}`);
  }

  getAllPrevisions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/previsions`);
  }

  createPrevision(prevision: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/previsions`, prevision);
  }
}
