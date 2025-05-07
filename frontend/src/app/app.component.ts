import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'facebook-clone';

  constructor() {
    console.log('JWT Token:', localStorage.getItem('jwt'));
  }
} 