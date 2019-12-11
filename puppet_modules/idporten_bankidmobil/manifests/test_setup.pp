class idporten_bankidmobil::test_setup inherits idporten_bankidmobil{

  include platform

  if ($platform::test_setup) {
    include manage_bids
    manage_bids::bid_config { $idporten_bankidmobil::application:
      application       => $idporten_bankidmobil::application
    }
  }
}