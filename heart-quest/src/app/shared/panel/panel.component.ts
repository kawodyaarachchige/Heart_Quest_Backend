import { Component, input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-panel',
  standalone: true,
  imports: [NgClass],
  templateUrl: './panel.component.html',
  styleUrl: './panel.component.scss',
})
export class PanelComponent {

  title = input<string>();
  sectionClass = input<string>();
}
