import { UserService } from '../service/user.service';
export declare class UserController {
    private readonly userService;
    constructor(userService: UserService);
    getUser(): Promise<any>;
}
