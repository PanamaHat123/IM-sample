import { Role } from '../model/entity/Role.entity';
export declare class RoleDao {
    private manager;
    constructor();
    queryRole(): Promise<Role>;
}
