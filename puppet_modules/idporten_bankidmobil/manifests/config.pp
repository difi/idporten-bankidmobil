class idporten_bankidmobil::config inherits idporten_bankidmobil {

  file { "${idporten_bankidmobil::install_dir}${idporten_bankidmobil::application}/${idporten_bankidmobil::artifact_id}.conf":
    ensure  => 'file',
    content => template("${module_name}/${idporten_bankidmobil::artifact_id}.conf.erb"),
    owner   => $idporten_bankidmobil::service_name,
    group   => $idporten_bankidmobil::service_name,
    mode    => '0444',
  } ->
  file { "${idporten_bankidmobil::config_root}${idporten_bankidmobil::application}/application.properties":
    ensure  => 'file',
    content => template("${module_name}/application.properties.erb"),
    owner   => $idporten_bankidmobil::service_name,
    group   => $idporten_bankidmobil::service_name,
    mode    => '0444',
  } ->
  file { "/etc/rc.d/init.d/${idporten_bankidmobil::service_name}":
    ensure => 'link',
    target => "${idporten_bankidmobil::install_dir}${idporten_bankidmobil::application}/${idporten_bankidmobil::artifact_id}.war",
  }
  difilib::logback_config { $idporten_bankidmobil::application:
    application       => $idporten_bankidmobil::application,
    owner             => $idporten_bankidmobil::service_name,
    group             => $idporten_bankidmobil::service_name,
    resilience        => false,
    performance_class => '',
    loglevel_no       => $idporten_bankidmobil::log_level,
    loglevel_nondifi  => $idporten_bankidmobil::log_level,
  } ->
  file { "${idporten_bankidmobil::config_root}${idporten_bankidmobil::application}/messages/idporten_en.properties":
    ensure => 'file',
    source => "puppet:///modules/${idporten_bankidmobil::module}/idporten_en.properties",
    owner  => $idporten_bankidmobil::service_name,
    group  => $idporten_bankidmobil::service_name,
  } ->
  file { "${idporten_bankidmobil::config_root}${idporten_bankidmobil::application}/messages/idporten_nn.properties":
    ensure => 'file',
    source => "puppet:///modules/${idporten_bankidmobil::module}/idporten_nn.properties",
    owner  => $idporten_bankidmobil::service_name,
    group  => $idporten_bankidmobil::service_name,
  } ->
  file { "${idporten_bankidmobil::config_root}${idporten_bankidmobil::application}/messages/idporten_se.properties":
    ensure => 'file',
    source => "puppet:///modules/${idporten_bankidmobil::module}/idporten_se.properties",
    owner  => $idporten_bankidmobil::service_name,
    group  => $idporten_bankidmobil::service_name,
  } ->
  file { "${idporten_bankidmobil::config_root}${idporten_bankidmobil::application}/messages/idporten_nb.properties":
    ensure => 'file',
    source => "puppet:///modules/${idporten_bankidmobil::module}/idporten_nb.properties",
    owner  => $idporten_bankidmobil::service_name,
    group  => $idporten_bankidmobil::service_name,
  }

}
