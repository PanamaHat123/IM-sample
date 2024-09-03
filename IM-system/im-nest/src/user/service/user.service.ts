import { Injectable } from '@nestjs/common';
import { User } from '../entity/User.entity';
import { InjectEntityManager } from '@nestjs/typeorm';
import { EntityManager } from 'typeorm';

@Injectable()
export class UserService {

    @InjectEntityManager()
    private manager: EntityManager;

    async getUser(): Promise<User> {
        return  this.manager.findOne(User,{
            where: {
                id: 1
            }
        });
    }


}
