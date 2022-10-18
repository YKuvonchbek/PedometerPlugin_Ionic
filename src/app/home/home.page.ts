import { Component } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core'

declare var Capacitor;
import { Plugins } from "@capacitor/core";
import { TestObject } from 'protractor/built/driverProviders';
// import { Console } from 'console';

const { EchoPlugin } = Plugins;
const { PedometerPlugin } = Plugins;



@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {

  public steps: any = "0";

  constructor(private ref: ChangeDetectorRef) {
    // const myPluginEventListener = await PedometerPlugin.addListener(
    //   'timerEvent',
    //   (info: any) => {
    //     console.log('myPluginEvent was fired');
    //   },
    // );
  }
  
  ngOnInit() {
    window.addEventListener('stepEvent', (event: any) => {
      console.log(event.numberOfSteps);
      this.steps = event.numberOfSteps;
      this.ref.detectChanges();
    });
}  
  
  async getSteps() {
    PedometerPlugin.start();
  }
}