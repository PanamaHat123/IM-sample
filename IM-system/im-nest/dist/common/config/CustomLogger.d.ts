import { LoggerService } from '@nestjs/common';
export declare class CustomLogger implements LoggerService {
    private logger;
    constructor();
    log(message: string, context: string): void;
    error(message: string, context: string): void;
    warn(message: string, context: string): void;
}
