import { Component } from '@angular/core';

declare var Capacitor;
import { Plugins } from "@capacitor/core";

const { EchoPlugin } = Plugins;
const { PedometerPlugin } = Plugins;

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {

  public steps: string = "0";

  constructor() {
  }

  async getSteps() {
    // let result = await PedometerPlugin.execute("startPedometerUpdates", {  });
    // this.steps = result;
    // let test = await EchoPlugin.echo ({ value: "something echo" });
    let pedo = await PedometerPlugin.start();
    this.steps = pedo["seconds"];
  }

  changeText($event) {
    this.steps = "test";
  }
}