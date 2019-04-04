package org.grouporga.java.back.end.api.config.security.oauth2;

import org.grouporga.java.back.end.api.security.OAuthClientDetailsService;
import org.grouporga.java.back.end.api.security.OrgaUserDetailsService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.inject.Inject;

/**
 * OAuth2 authorization server configuration.
 */
@Configuration
@EnableAuthorizationServer
public class OAuthAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

  private final AuthenticationManager authenticationManager;
  private final TokenStore tokenStore;
  private final TokenEnhancer tokenEnhancer;
  private final OrgaUserDetailsService userDetailsService;
  private final OAuthClientDetailsService oAuthClientDetailsService;

  @Inject
  public OAuthAuthorizationServerConfig(AuthenticationManager authenticationManager, TokenStore tokenStore,
                                        TokenEnhancer tokenEnhancer, OrgaUserDetailsService userDetailsService,
                                        OAuthClientDetailsService oAuthClientDetailsService) {
    this.authenticationManager = authenticationManager;
    this.tokenStore = tokenStore;
    this.tokenEnhancer = tokenEnhancer;
    this.userDetailsService = userDetailsService;
    this.oAuthClientDetailsService = oAuthClientDetailsService;
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oAuthServer) {
    final OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
    oAuth2AuthenticationEntryPoint.setExceptionRenderer(new JsonApiOauthExceptionRenderer());
    oAuthServer
      .tokenKeyAccess("isAnonymous() || hasAuthority('ROLE_TRUSTED_CLIENT')")
      .checkTokenAccess("hasAuthority('ROLE_TRUSTED_CLIENT')")
      .authenticationEntryPoint(oAuth2AuthenticationEntryPoint)
      .allowFormAuthenticationForClients();
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.withClientDetails(oAuthClientDetailsService);
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    endpoints
      .userDetailsService(userDetailsService)
      .tokenStore(tokenStore)
      .tokenEnhancer(tokenEnhancer)
      .authenticationManager(authenticationManager);
  }
}
