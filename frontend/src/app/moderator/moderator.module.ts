import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ModeratorRoutingModule } from './moderator-routing.module';
import { ModeratorPanelComponent } from './components/moderator-panel/moderator-panel.component';


@NgModule({
  declarations: [
    ModeratorPanelComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ModeratorRoutingModule
  ]
})
export class ModeratorModule { }
