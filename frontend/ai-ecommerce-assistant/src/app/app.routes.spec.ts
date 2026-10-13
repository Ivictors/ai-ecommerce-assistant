import { routes } from './app.routes';

describe('application routes', () => {
  it('T-002-T1 AC-007 exposes a login route', () => {
    expect(routes.some((route) => route.path === 'login')).toBe(true);
  });

  it('T-002-T2 AC-001 resolves the standalone login component', async () => {
    const loginRoute = routes.find((route) => route.path === 'login');
    expect(loginRoute?.loadComponent).toEqual(expect.any(Function));
    await expect(loginRoute?.loadComponent?.()).resolves.toBeTruthy();
  });

  it('T-002-T3 AC-007 sends the public entry route to login', () => {
    expect(routes.find((route) => route.path === '')?.redirectTo).toBe('login');
  });
});
