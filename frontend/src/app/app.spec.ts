import { TestBed } from '@angular/core/testing';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App]
    }).compileComponents();
  });

  it('renders the application shell', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('mat-toolbar')?.textContent).toContain('Agenda KJG');
    expect(compiled.querySelector('.eyebrow')?.textContent).toContain(
      'KJG Repostería y Manualidades'
    );
    expect(compiled.querySelector('h1')?.textContent).toContain('Agenda KJG');
  });
});
