import { bootstrapApplication } from '@angular/platform-browser';
import { registerLocaleData } from '@angular/common';
import localeEsMx from '@angular/common/locales/es-MX';
import { APP_LOCALE, appConfig } from './app/app.config';
import { App } from './app/app';

registerLocaleData(localeEsMx, APP_LOCALE);

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
