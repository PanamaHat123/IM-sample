import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { NestExpressApplication } from '@nestjs/platform-express'
import { CustomLogger } from './common/config/CustomLogger';

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(AppModule,{
    logger: new CustomLogger()
  });

  app.useStaticAssets('public');

  await app.listen(3000);
}
bootstrap();
