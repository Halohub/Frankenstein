CREATE DATABASE IF NOT EXISTS frankenstein DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'Frankenstein application database';
USE frankenstein;

-- ---------------------------------------------------------------------------
-- Admin accounts (back-office)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_admin (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    username        VARCHAR(64)  NOT NULL                COMMENT 'Login username, unique',
    password        VARCHAR(128) NOT NULL                COMMENT 'BCrypt password hash',
    nickname        VARCHAR(64)  DEFAULT NULL           COMMENT 'Display name',
    phone           VARCHAR(20)  DEFAULT NULL           COMMENT 'Mobile phone number',
    email           VARCHAR(128) DEFAULT NULL           COMMENT 'Email address',
    status          TINYINT      NOT NULL DEFAULT 1     COMMENT 'Account status: 0=disabled, 1=active',
    last_login_time DATETIME     DEFAULT NULL           COMMENT 'Last successful login time',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update time',
    deleted         TINYINT      NOT NULL DEFAULT 0     COMMENT 'Logical delete flag: 0=not deleted, 1=deleted',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Back-office administrator accounts';

-- ---------------------------------------------------------------------------
-- Member accounts (mall / user client)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_member (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    username        VARCHAR(64)  NOT NULL                COMMENT 'Login username, unique',
    password        VARCHAR(128) NOT NULL                COMMENT 'BCrypt password hash',
    nickname        VARCHAR(64)  DEFAULT NULL           COMMENT 'Display name',
    phone           VARCHAR(20)  DEFAULT NULL           COMMENT 'Mobile phone number, unique when set',
    email           VARCHAR(128) DEFAULT NULL           COMMENT 'Email address, unique when set',
    vip_level       TINYINT      NOT NULL DEFAULT 0     COMMENT 'VIP level: 0=normal, 1=vip (affects max login devices)',
    status          TINYINT      NOT NULL DEFAULT 1     COMMENT 'Account status: 0=disabled, 1=active',
    register_source VARCHAR(32)  DEFAULT 'web'          COMMENT 'Registration channel, e.g. web, app, h5',
    last_login_time DATETIME     DEFAULT NULL           COMMENT 'Last successful login time',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update time',
    deleted         TINYINT      NOT NULL DEFAULT 0     COMMENT 'Logical delete flag: 0=not deleted, 1=deleted',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_username (username),
    UNIQUE KEY uk_member_phone (phone),
    UNIQUE KEY uk_member_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Mall member / end-user accounts';

-- ---------------------------------------------------------------------------
-- Roles (scoped to ADMIN or MEMBER)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    role_code   VARCHAR(64)  NOT NULL                COMMENT 'Role code, unique, e.g. ADMIN_SUPER',
    role_name   VARCHAR(64)  NOT NULL                COMMENT 'Human-readable role name',
    role_scope  VARCHAR(16)  NOT NULL                COMMENT 'Applicable account scope: ADMIN or MEMBER',
    status      TINYINT      NOT NULL DEFAULT 1     COMMENT 'Role status: 0=disabled, 1=active',
    remark      VARCHAR(255) DEFAULT NULL           COMMENT 'Optional description',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update time',
    deleted     TINYINT      NOT NULL DEFAULT 0     COMMENT 'Logical delete flag: 0=not deleted, 1=deleted',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RBAC roles for admin and member accounts';

-- ---------------------------------------------------------------------------
-- Permissions (menu / button / API)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_permission (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    perm_code   VARCHAR(128) NOT NULL                COMMENT 'Permission code, format scope:module:action',
    perm_name   VARCHAR(64)  NOT NULL                COMMENT 'Default permission name (English fallback)',
    perm_type   VARCHAR(16)  NOT NULL DEFAULT 'API'  COMMENT 'Permission type: MENU, BUTTON, or API',
    parent_id   BIGINT       DEFAULT 0               COMMENT 'Parent permission ID for tree structure, 0=root',
    path        VARCHAR(255) DEFAULT NULL           COMMENT 'Frontend route path for MENU permissions',
    component   VARCHAR(255) DEFAULT NULL           COMMENT 'Vue component path; # layout, ## parent layout',
    icon        VARCHAR(64)  DEFAULT NULL           COMMENT 'Menu icon identifier',
    hidden      TINYINT      NOT NULL DEFAULT 0     COMMENT 'Hide from sidebar: 0=visible, 1=hidden',
    sort        INT          DEFAULT 0               COMMENT 'Display sort order, ascending',
    status      TINYINT      NOT NULL DEFAULT 1     COMMENT 'Permission status: 0=disabled, 1=active',
    api_path    VARCHAR(255) DEFAULT NULL           COMMENT 'Backend API path for API permissions',
    method      VARCHAR(16)  DEFAULT NULL           COMMENT 'HTTP method for API permissions, e.g. GET, POST',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update time',
    deleted     TINYINT      NOT NULL DEFAULT 0     COMMENT 'Logical delete flag: 0=not deleted, 1=deleted',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RBAC permissions for menus, buttons, and APIs';

-- ---------------------------------------------------------------------------
-- Business i18n messages
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_i18n_message (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    ref_type    VARCHAR(32)  NOT NULL                COMMENT 'Reference type, e.g. PERMISSION',
    ref_id      BIGINT       NOT NULL                COMMENT 'Referenced entity id',
    locale      VARCHAR(16)  NOT NULL                COMMENT 'Language tag: en, zh, ja',
    field_name  VARCHAR(64)  NOT NULL                COMMENT 'Translated field name',
    field_value VARCHAR(512) NOT NULL               COMMENT 'Translated text',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_i18n_ref (ref_type, ref_id, locale, field_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Business i18n messages for dynamic content';

-- ---------------------------------------------------------------------------
-- Role-permission mapping
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    role_id       BIGINT NOT NULL                COMMENT 'Foreign key to sys_role.id',
    permission_id BIGINT NOT NULL                COMMENT 'Foreign key to sys_permission.id',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Many-to-many mapping between roles and permissions';

-- ---------------------------------------------------------------------------
-- Admin-role mapping
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_admin_role (
    id       BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    admin_id BIGINT NOT NULL                COMMENT 'Foreign key to sys_admin.id',
    role_id  BIGINT NOT NULL                COMMENT 'Foreign key to sys_role.id (role_scope must be ADMIN)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_role (admin_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Many-to-many mapping between admin accounts and roles';

-- ---------------------------------------------------------------------------
-- Member-role mapping
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_member_role (
    id        BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    member_id BIGINT NOT NULL                COMMENT 'Foreign key to sys_member.id',
    role_id   BIGINT NOT NULL                COMMENT 'Foreign key to sys_role.id (role_scope must be MEMBER)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_role (member_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Many-to-many mapping between member accounts and roles';

-- ---------------------------------------------------------------------------
-- Seed data
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO sys_role (id, role_code, role_name, role_scope, remark) VALUES
(1, 'ADMIN_SUPER', 'Super Admin', 'ADMIN', 'Full admin access'),
(2, 'MEMBER_NORMAL', 'Normal Member', 'MEMBER', 'Default member role'),
(3, 'ADMIN_OPERATOR', 'Operator Admin', 'ADMIN', 'Example admin role: back-office access without system/role management');

INSERT IGNORE INTO sys_permission
    (id, perm_code, perm_name, perm_type, parent_id, path, component, icon, hidden, sort, api_path, method) VALUES
(1,  'admin:auth:info',              'Auth Info',        'API',    0,  NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/auth/info',          'GET'),
(2,  'admin:auth:menus',             'Auth Menus',       'API',    0,  NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/auth/menus',         'GET'),
(10, 'admin:system',                 'System',           'MENU',   0,  '/system', '#',                       'ep:setting',  0, 10, NULL,                               NULL),
(11, 'admin:system:role',            'Roles',            'MENU',   10, 'role',   'views/system/role/index', 'ep:user',     0, 1,  NULL,                               NULL),
(12, 'admin:system:role:add',        'Add Role',         'BUTTON', 11, NULL,     NULL,                      NULL,          0, 1,  NULL,                               NULL),
(13, 'admin:system:role:edit',       'Edit Role',        'BUTTON', 11, NULL,     NULL,                      NULL,          0, 2,  NULL,                               NULL),
(14, 'admin:system:role:delete',     'Delete Role',      'BUTTON', 11, NULL,     NULL,                      NULL,          0, 3,  NULL,                               NULL),
(20, 'admin:system:role:list',       'List Roles',       'API',    11, NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/role',               'GET'),
(21, 'admin:system:role:detail',     'Role Detail',      'API',    11, NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/role/*',             'GET'),
(22, 'admin:system:role:create',     'Create Role',      'API',    11, NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/role',               'POST'),
(23, 'admin:system:role:update',     'Update Role',      'API',    11, NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/role/*',             'PUT'),
(24, 'admin:system:role:delete',     'Delete Role',      'API',    11, NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/role/*',             'DELETE'),
(30, 'admin:system:permission:tree','Permission Tree',  'API',    11, NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/permission/tree',    'GET'),
(40, 'admin:member',                 'Members',          'MENU',   0,  '/member', '#',                       'ep:user-filled', 0, 20, NULL,                            NULL),
(41, 'admin:member:list',            'Member List',      'MENU',   40, 'list',   'views/member/list/index', 'ep:list',       0, 1,  NULL,                               NULL),
(42, 'admin:member:view',            'View Member',      'BUTTON', 41, NULL,     NULL,                      NULL,          0, 1,  NULL,                               NULL),
(43, 'admin:member:add',             'Add Member',       'BUTTON', 41, NULL,     NULL,                      NULL,          0, 2,  NULL,                               NULL),
(44, 'admin:member:edit',            'Edit Member',      'BUTTON', 41, NULL,     NULL,                      NULL,          0, 3,  NULL,                               NULL),
(45, 'admin:member:delete',          'Delete Member',    'BUTTON', 41, NULL,     NULL,                      NULL,          0, 4,  NULL,                               NULL),
(50, 'admin:member:query',           'Query Members',    'API',    41, NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/member/list',        'GET'),
(51, 'admin:member:detail',          'Member Detail',    'API',    41, NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/member/*',           'GET'),
(52, 'admin:member:create',          'Create Member',    'API',    41, NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/member',             'POST'),
(53, 'admin:member:update',          'Update Member',    'API',    41, NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/member/*',           'PUT'),
(54, 'admin:member:remove',          'Remove Member',    'API',    41, NULL,     NULL,                      NULL,          0, 0,  '/admin/api_v1/member/*',           'DELETE'),
(3,  'member:auth:info',             'Member Auth Info', 'API',    0,  NULL,     NULL,                      NULL,          0, 0,  '/user/api_v1/auth/info',           'GET'),
(4,  'member:order:view',            'View Orders',      'API',    0,  NULL,     NULL,                      NULL,          0, 0,  '/user/api_v1/order/list',          'GET');

INSERT IGNORE INTO sys_i18n_message (ref_type, ref_id, locale, field_name, field_value) VALUES
('PERMISSION', 10, 'zh', 'perm_name', '系统管理'),
('PERMISSION', 10, 'ja', 'perm_name', 'システム'),
('PERMISSION', 11, 'zh', 'perm_name', '角色管理'),
('PERMISSION', 11, 'ja', 'perm_name', 'ロール管理'),
('PERMISSION', 40, 'zh', 'perm_name', '会员管理'),
('PERMISSION', 40, 'ja', 'perm_name', '会員管理'),
('PERMISSION', 41, 'zh', 'perm_name', '会员列表'),
('PERMISSION', 41, 'ja', 'perm_name', '会員一覧');

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE perm_code LIKE 'admin:%';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE perm_code LIKE 'member:%';

-- ADMIN_OPERATOR: auth + member read-only (no system/role management, no member write)
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
(3, 1),
(3, 2),
(3, 40),
(3, 41),
(3, 42),
(3, 50),
(3, 51);


INSERT IGNORE INTO sys_admin (id, username, password, nickname, status) VALUES
(1, 'admin', '$2a$10$DRFSjoDz9NXMt8RQJlvCD.YVRZNmFPWMVzL3NkYKREXbmCHz9TovW', 'Super Admin', 1);

INSERT IGNORE INTO sys_admin_role (admin_id, role_id) VALUES (1, 1);


INSERT IGNORE INTO sys_admin (id, username, password, nickname, status) VALUES
(2, 'operator', '$2a$10$DRFSjoDz9NXMt8RQJlvCD.YVRZNmFPWMVzL3NkYKREXbmCHz9TovW', 'Operator Admin', 1);

INSERT IGNORE INTO sys_admin_role (admin_id, role_id) VALUES (2, 3);
