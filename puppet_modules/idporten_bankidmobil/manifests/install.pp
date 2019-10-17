class idporten_bankidmobil::install inherits idporten_bankidmobil {

  user { $idporten_bankidmobil::service_name:
    ensure => present,
    shell  => '/sbin/nologin',
    home   => '/',
  } ->
  file { $idporten_bankidmobil::idporten_install_dir:
    ensure  => 'directory',
    replace => false,
    owner   => $idporten_bankidmobil::service_name,
    group   => $idporten_bankidmobil::service_name,
  }
  file { "${idporten_bankidmobil::config_root}${idporten_bankidmobil::application}":
    ensure => 'directory',
    owner  => $idporten_bankidmobil::service_name,
    group  => $idporten_bankidmobil::service_name,
    mode   => '0755',
  } ->
  file { "${idporten_bankidmobil::log_root}${idporten_bankidmobil::application}":
    ensure => 'directory',
    owner  => $idporten_bankidmobil::service_name,
    group  => $idporten_bankidmobil::service_name,
    mode   => '0755',
  }
  file { "${idporten_bankidmobil::config_root}${idporten_bankidmobil::application}/messages":
    ensure => 'directory',
    owner  => $idporten_bankidmobil::service_name,
    group  => $idporten_bankidmobil::service_name,
    mode   => '0755',
  }
}