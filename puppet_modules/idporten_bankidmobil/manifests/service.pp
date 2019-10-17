# service.pp
class idporten_bankidmobil::service inherits idporten_bankidmobil {

  include platform

  if ($platform::deploy_spring_boot) {
    service { $idporten_bankidmobil::service_name:
      ensure => running,
      enable => true,
    }
  }

}
