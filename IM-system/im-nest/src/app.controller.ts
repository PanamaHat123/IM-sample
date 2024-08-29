import { Controller, Get, Query } from '@nestjs/common';
import { AppService } from './app.service';
import { ModuleRef } from '@nestjs/core';

@Controller()
export class AppController {

  // appService: AppService = new AppService;
  constructor(private readonly appService: AppService,private moduleRef:ModuleRef) {}

  @Get("/hello")
  getHello(@Query() req): string {
  //  console.log( );
    return this.moduleRef.get(AppService).getHello(req);
  }

  @Get("/hi")
  getHi(): string {
    return "hi";
  }

}
