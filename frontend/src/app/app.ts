import { Component } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterOutlet } from '@angular/router';

const APPLICATION_NAME = 'Agenda KJG' as const;
const BUSINESS_DISPLAY_NAME = 'KJG Repostería y Manualidades' as const;

@Component({
  selector: 'app-root',
  imports: [MatToolbarModule, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly applicationName = APPLICATION_NAME;
  protected readonly businessDisplayName = BUSINESS_DISPLAY_NAME;
}
