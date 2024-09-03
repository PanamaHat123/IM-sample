"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.CustomLogger = void 0;
const winston_1 = require("winston");
const DailyRotateFile = require('winston-daily-rotate-file');
const chalk = require('chalk');
class CustomLogger {
    constructor() {
        this.logger = (0, winston_1.createLogger)({
            level: 'debug',
            format: winston_1.format.combine(winston_1.format.colorize(), winston_1.format.simple()),
            transports: [
                new winston_1.transports.Console({
                    format: winston_1.format.combine(winston_1.format.colorize(), winston_1.format.printf(({ context, level, message, time }) => {
                        const appStr = chalk.green(`[NEST]`);
                        const contextStr = chalk.yellow(`[${context}]`);
                        return `${appStr} ${time} ${level} ${contextStr} ${message} `;
                    }))
                }),
                new DailyRotateFile({
                    level: 'info',
                    dirname: 'log',
                    filename: 'nest-%DATE%.log',
                    datePattern: 'YYYY-MM-DD-HH-mm',
                    maxSize: '100k'
                })
            ]
        });
    }
    log(message, context) {
        this.logger.log('info', `[${context}] ${message}`);
    }
    error(message, context) {
        this.logger.log('error', `[${context}] ${message}`);
    }
    warn(message, context) {
        this.logger.log('warn', `[${context}] ${message}`);
    }
}
exports.CustomLogger = CustomLogger;
//# sourceMappingURL=CustomLogger.js.map