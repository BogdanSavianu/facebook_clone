import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ModeratorPanelComponent } from './components/moderator-panel/moderator-panel.component';

const routes: Routes = [
  {
    path: 'panel',
    component: ModeratorPanelComponent
  },
  {
    path: '',
    redirectTo: 'panel',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ModeratorRoutingModule { }
