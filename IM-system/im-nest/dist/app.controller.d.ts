import { AppService } from './app.service';
import { ModuleRef } from '@nestjs/core';
export declare class AppController {
    private readonly appService;
    private moduleRef;
    constructor(appService: AppService, moduleRef: ModuleRef);
    getHello(req: any): string;
    getHi(): string;
}
