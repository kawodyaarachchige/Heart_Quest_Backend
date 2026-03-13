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
  /** Optional section title (e.g. "Top 3", "Ranking") */
  title = input<string>();
  /** Optional extra class for the section (e.g. "podium-section", "ranking-section") */
  sectionClass = input<string>();
}
