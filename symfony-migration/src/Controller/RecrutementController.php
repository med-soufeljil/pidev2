<?php

namespace App\Controller;

use App\Entity\Recrutement;
use App\Form\RecrutementType;
use App\Repository\RecrutementRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/recrutements')]
class RecrutementController extends AbstractController
{
    #[Route('', name: 'app_recrutement_index')]
    public function index(RecrutementRepository $repository): Response
    {
        return $this->render('recrutement/index.html.twig', ['items' => $repository->findAll()]);
    }

    #[Route('/new', name: 'app_recrutement_new')]
    public function create(Request $request, EntityManagerInterface $em): Response
    {
        $item = new Recrutement();
        $form = $this->createForm(RecrutementType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($item);
            $em->flush();
            return $this->redirectToRoute('app_recrutement_index');
        }

        return $this->render('recrutement/form.html.twig', ['form' => $form]);
    }
}
