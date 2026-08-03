import {
  ApplicationConfig,
  DEFAULT_CURRENCY_CODE,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners
} from '@angular/core';
import { provideHttpClient, withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { initializeAuth } from './auth/auth-initializer';
import { authInterceptor } from './auth/auth.interceptor';
import { routes } from './app.routes';

export const APP_LOCALE = 'es-MX' as const;
export const APP_DEFAULT_CURRENCY_CODE = 'MXN' as const;

export const appConfig: ApplicationConfig = {
  providers: [
    { provide: LOCALE_ID, useValue: APP_LOCALE },
    { provide: DEFAULT_CURRENCY_CODE, useValue: APP_DEFAULT_CURRENCY_CODE },
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(
      withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' }),
      withInterceptors([authInterceptor])
    ),
    provideRouter(routes),
    provideAppInitializer(initializeAuth)
  ]
};
