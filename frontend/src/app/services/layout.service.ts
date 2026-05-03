import { Injectable, signal, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export type Theme = 'dark' | 'light';

@Injectable({
  providedIn: 'root'
})
export class LayoutService {
  private platformId = inject(PLATFORM_ID);
  
  theme = signal<Theme>('dark');
  isSidebarOpenMobile = signal<boolean>(false);
  isSidebarCollapsedDesktop = signal<boolean>(false);

  constructor() {
    this.initTheme();
  }

  private initTheme() {
    if (isPlatformBrowser(this.platformId)) {
      const savedTheme = localStorage.getItem('app-theme') as Theme;
      if (savedTheme === 'light' || savedTheme === 'dark') {
        this.theme.set(savedTheme);
      } else {
        this.theme.set('dark');
      }
      this.applyThemeToBody();
    }
  }

  toggleTheme() {
    const newTheme = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(newTheme);
    
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('app-theme', newTheme);
      this.applyThemeToBody();
    }
  }

  private applyThemeToBody() {
    if (isPlatformBrowser(this.platformId)) {
      const body = document.body;
      if (this.theme() === 'light') {
        body.setAttribute('data-theme', 'light');
      } else {
        body.removeAttribute('data-theme');
      }
    }
  }

  toggleSidebarMobile() {
    this.isSidebarOpenMobile.update(v => !v);
  }

  closeSidebarMobile() {
    this.isSidebarOpenMobile.set(false);
  }

  toggleSidebarDesktop() {
    this.isSidebarCollapsedDesktop.update(v => !v);
  }
}
