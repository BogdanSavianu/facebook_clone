import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './auth/components/login/login.component';
import { RegisterComponent } from './auth/components/register/register.component';
import { HomeComponent } from './home/components/home/home.component';
import { PostDetailComponent } from './post/components/post-detail/post-detail.component';
import { PostFormComponent } from './post/components/post-form/post-form.component';
import { CommentEditComponent } from './comment/components/comment-edit/comment-edit.component';
import { AuthGuard } from './auth/services/auth.guard';
import { RoleGuard } from './auth/services/role.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: '', component: HomeComponent, canActivate: [AuthGuard] },
  { path: 'posts/create', component: PostFormComponent, canActivate: [AuthGuard] },
  { path: 'posts/edit/:id', component: PostFormComponent, canActivate: [AuthGuard] },
  { path: 'posts/:id', component: PostDetailComponent, canActivate: [AuthGuard] },
  { path: 'comments/edit/:id', component: CommentEditComponent, canActivate: [AuthGuard] },
  {
    path: 'moderator',
    loadChildren: () => import('./moderator/moderator.module').then(m => m.ModeratorModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_MODERATOR', 'ROLE_ADMIN'] }
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { } 