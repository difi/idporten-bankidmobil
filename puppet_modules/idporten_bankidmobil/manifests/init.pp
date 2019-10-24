#init.pp
class idporten_bankidmobil (
  $bankid_webaddress_hostname                    =$idporten_bankidmobil::params::bankid_webaddress_hostname,
  $bankid_servlet_hostname                       =$idporten_bankidmobil::params::bankid_servlet_hostname,
  $bankid_webaddress_ip                          =$idporten_bankidmobil::params::bankid_webaddress_ip,
  $node_environment                              =$idporten_bankidmobil::params::node_environment,
  $bankid_action                                 =$idporten_bankidmobil::params::bankid_action,
  $bankid_clienttype                             =$idporten_bankidmobil::params::bankid_clienttype,
  $bankid_keystore_file                          =$idporten_bankidmobil::params::bankid_keystore_file,
  $bankid_keystore_password                      =$idporten_bankidmobil::params::bankid_keystore_password,
  $bankid_merchant_name                          =$idporten_bankidmobil::params::bankid_merchant_name,
  $bankid_grantedpolicies                        =$idporten_bankidmobil::params::bankid_grantedpolicies,
  $bankid_logging_enabled                        =$idporten_bankidmobil::params::bankid_logging_enabled,
  $bankid_logging_prop_file                      =$idporten_bankidmobil::params::bankid_logging_prop_file,
  $bankid_logging_category_name                  =$idporten_bankidmobil::params::bankid_logging_category_name,
  $bankid_ocsp_max_time_skew                     =$idporten_bankidmobil::params::bankid_ocsp_max_time_skew,
  $bankid_cors_allow_origin                      =$idporten_bankidmobil::params::bankid_cors_allow_origin,
  $bankid_suppress_broadcast                     =$idporten_bankidmobil::params::bankid_suppress_broadcast,
  $bankidmobil_basic_username                    =$idporten_bankidmobil::params::bankidmobil_basic_username,
  $bankidmobil_basic_password                    =$idporten_bankidmobil::params::bankidmobil_basic_password,
  $mvc_async_request_timeout                     =$idporten_bankidmobil::params::mvc_async_request_timeout,
  $config_root                                   =$idporten_bankidmobil::params::config_root,
  $log_root                                      =$idporten_bankidmobil::params::log_root,
  $log_level                                     =$idporten_bankidmobil::params::log_level,
  $module                                        =$idporten_bankidmobil::params::module,
  $application                                   =$idporten_bankidmobil::params::application,
  $context                                       =$idporten_bankidmobil::params::context,
  $install_dir                                   =$idporten_bankidmobil::params::install_dir,
  $server_port                                   =$idporten_bankidmobil::params::server_port,
  $idporten_redirect_url                         =$idporten_bankidmobil::params::idporten_redirect_url,
  $java_home                                     =$idporten_bankidmobil::params::java_home
) inherits idporten_bankidmobil::params {

  include platform


  anchor { 'idporten_bankidmobil::begin': } ->
  class { '::idporten_bankidmobil::install': } ->
  class { '::idporten_bankidmobil::deploy': } ->
  class { '::idporten_bankidmobil::config': } ~>
  class { '::idporten_bankidmobil::test_setup': } ->
  class { '::idporten_bankidmobil::service': } ->
  anchor { 'idporten_bankidmobil::end': }
}
