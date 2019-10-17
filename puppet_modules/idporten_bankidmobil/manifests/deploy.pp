class idporten_bankidmobil::deploy inherits idporten_bankidmobil {

  include 'difilib'

  difilib::spring_boot_deploy { $idporten_bankidmobil::application:
    package       => 'no.idporten.bankid',
    artifact      => $idporten_bankidmobil::artifact_id,
    service_name  => $idporten_bankidmobil::service_name,
    install_dir   => "${idporten_bankidmobil::install_dir}${idporten_bankidmobil::application}",
    artifact_type => "war",
  }
}


