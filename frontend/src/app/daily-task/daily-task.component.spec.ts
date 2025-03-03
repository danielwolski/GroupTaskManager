import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DailyTaskComponent } from './daily-task.component';

describe('DailyTaskComponent', () => {
  let component: DailyTaskComponent;
  let fixture: ComponentFixture<DailyTaskComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DailyTaskComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DailyTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
