import { Injectable } from '@nestjs/common';

@Injectable()
export class AppService {
  getHello(parms:object): string {
    return 'Hello World!'+JSON.stringify(parms);
  }
}
