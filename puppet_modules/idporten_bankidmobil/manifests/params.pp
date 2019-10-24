#params.pp
class idporten_bankidmobil::params {
  $artifact_id                                   = 'idporten-bankidmobil'
  $service_name                                  = 'idporten-bankidmobil'
  $server_port                                   = 10004
  $bankid_servlet_hostname                       = hiera('platform::external_host')
  $bankid_webaddress_hostname                    = hiera('platform::external_host')
  $bankid_webaddress_ip                          = hiera('common::internet_ip')
  $node_environment                              = hiera('platform::node_environment', '')
  $bankid_action                                 = 'auth'
  $bankid_clienttype                             = 'NC'
  $bankid_keystore_file                          = '/etc/opt/idporten-bankidmobil/ID-Porten-BINAS.bid'
  $bankid_keystore_password                      = 'qwer1234'
  $bankid_merchant_name                          = 'ID-Porten-BINAS'
  $bankid_grantedpolicies                        = 'ALL'
  $bankid_logging_enabled                        = false
  $bankid_logging_prop_file                      = '/etc/opt/idporten-bankidmobil/bankid.log4j.properties'
  $bankid_logging_category_name                  = 'bankid'
  $bankid_ocsp_max_time_skew                     = 240000
  $bankid_cors_allow_origin                      = 'https://csfe-preprod.bankid.no'
  $bankid_suppress_broadcast                     = 'N'
  $bankidmobil_basic_username                    = 'user'
  $bankidmobil_basic_password                    = 'password'
  $mvc_async_request_timeout                     = 180000
  $idporten_install_dir                          = '/opt/idporten-bankidmobil'
  $config_root                                   = '/etc/opt/'
  $log_root                                      = '/var/log/'
  $install_dir                                   = '/opt/'
  $module                                        = 'idporten_bankidmobil'
  $application                                   = 'idporten-bankidmobil'
  $context                                       = 'opensso'
  $setup_openam                                  = false
  $idporten_redirect_url                         = 'http://localhost:8080/redirecturl'
  $java_home                                     = hiera('platform::java_home')
  $eventsource_enabled                           = true
}
