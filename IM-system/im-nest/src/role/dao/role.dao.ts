import { Injectable } from '@nestjs/common';
import { InjectEntityManager } from '@nestjs/typeorm';
import { EntityManager } from 'typeorm';
import { Role } from '../model/entity/Role.entity';

@Injectable()
export class RoleDao {

    @InjectEntityManager()
    private manager: EntityManager;
        
    constructor() { }

    async queryRole(): Promise<Role> {
        return  this.manager.findOne(Role,{
            where: {
                id: 1
            }
        });
    }

}
