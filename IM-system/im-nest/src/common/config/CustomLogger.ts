import { LoggerService } from '@nestjs/common'
import { createLogger, format, Logger, transports } from 'winston'
const DailyRotateFile = require('winston-daily-rotate-file')
const chalk = require('chalk')


export class CustomLogger implements LoggerService {
  private logger: Logger

  constructor() {

    this.logger = createLogger({
      level: 'debug',
      format: format.combine(format.colorize(), format.simple()),
      transports: [
        new transports.Console({
            format: format.combine(
              format.colorize(),
              format.printf(({ context, level, message, time }) => {
                const appStr = chalk.green(`[NEST]`)
                const contextStr = chalk.yellow(`[${context}]`)
          
                return `${appStr} ${time} ${level} ${contextStr} ${message} `
              })
            )
          }),
        new DailyRotateFile({
            level: 'info',
            dirname: 'log',
            filename: 'nest-%DATE%.log',
            datePattern: 'YYYY-MM-DD-HH-mm',
            maxSize: '100k'
          })
      ]
    })
  }

  log(message: string, context: string) {
    this.logger.log('info', `[${context}] ${message}`)
  }

  error(message: string, context: string) {
    this.logger.log('error', `[${context}] ${message}`)
  }

  warn(message: string, context: string) {
    this.logger.log('warn', `[${context}] ${message}`)
  }
}
