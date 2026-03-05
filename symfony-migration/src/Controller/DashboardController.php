<?php

namespace App\Controller;

use App\Repository\CandidatRepository;
use App\Repository\OffreRepository;
use App\Repository\RecrutementRepository;
use App\Repository\ReunionRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class DashboardController extends AbstractController
{
    #[Route('/', name: 'app_dashboard')]
    public function index(
        CandidatRepository $candidats,
        OffreRepository $offres,
        RecrutementRepository $recrutements,
        ReunionRepository $reunions
    ): Response {
        return $this->render('dashboard/index.html.twig', [
            'counts' => [
                'candidats' => $candidats->count([]),
                'offres' => $offres->count([]),
                'recrutements' => $recrutements->count([]),
                'reunions' => $reunions->count([]),
            ],
        ]);
    }
}
